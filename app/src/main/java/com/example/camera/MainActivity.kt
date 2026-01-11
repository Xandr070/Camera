package com.example.camera

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.camera.fragment.GalleryFragment
import com.example.camera.fragment.PhotoFragment
import com.example.camera.fragment.VideoFragment
import com.example.camera.ui.components.BottomNavigationBar
import com.example.camera.ui.components.GalleryButton
import com.example.camera.ui.components.NavigationItem
import com.example.camera.ui.theme.CameraXAppTheme
import com.example.camera.utils.Permissions
import com.example.camera.utils.allGranted
import com.example.camera.utils.deniedList
import com.example.camera.utils.hasAllPermissions
import com.example.camera.viewmodel.GalleryViewModel
import com.example.camera.viewmodel.PhotoViewModel
import com.example.camera.viewmodel.VideoViewModel
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    private var hasPermissions by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.allGranted()) {
            hasPermissions = true
        } else {
            Toast.makeText(
                baseContext,
                "Требуются разрешения: ${results.deniedList()}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasPermissions = baseContext.hasAllPermissions()

        val imageViewModel: PhotoViewModel by viewModels()
        val videoViewModel: VideoViewModel by viewModels()
        val libraryViewModel: GalleryViewModel by viewModels()

        val tabs = listOf(Destination.Photo, Destination.Video, Destination.Library)

        enableEdgeToEdge()
        setContent {
            val navigationStack = remember { mutableStateListOf<Destination>(Destination.Photo) }

            CameraXAppTheme {
                LaunchedEffect(Unit) {
                    if (!hasPermissions && !baseContext.hasAllPermissions()) {
                        requestPermissions()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!hasPermissions) {
                        PermissionRequestScreen(
                            modifier = Modifier.fillMaxSize(),
                            onRequestClick = { requestPermissions() }
                        )
                    } else {
                        NavDisplay(
                            modifier = Modifier.fillMaxSize(),
                            backStack = navigationStack,
                            onBack = { navigationStack.removeLastOrNull() },
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            entryProvider = entryProvider {
                                entry<Destination.Photo> {
                                    PhotoFragment(
                                        modifier = Modifier.fillMaxSize(),
                                        paddings = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                        viewModel = imageViewModel
                                    )
                                }
                                entry<Destination.Video> {
                                    VideoFragment(
                                        modifier = Modifier.fillMaxSize(),
                                        paddings = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                        viewModel = videoViewModel
                                    )
                                }
                                entry<Destination.Library> {
                                    GalleryFragment(
                                        modifier = Modifier.fillMaxSize(),
                                        paddings = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                        viewModel = libraryViewModel
                                    )
                                }
                            }
                        )

                        val currentDestination = navigationStack.lastOrNull() ?: Destination.Photo
                        val currentIndex = when (currentDestination) {
                            is Destination.Photo -> 0
                            is Destination.Video -> 1
                            else -> 0
                        }

                        val navItems = listOf(
                            NavigationItem("Фото"),
                            NavigationItem("Видео")
                        )

                        BottomNavigationBar(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            selectedIndex = currentIndex,
                            items = navItems,
                            onItemSelected = { index ->
                                navigationStack.clear()
                                navigationStack.add(tabs[index])
                            }
                        )

                        GalleryButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 100.dp, end = 24.dp),
                            onClick = {
                                navigationStack.clear()
                                navigationStack.add(Destination.Library)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(Permissions.required)
    }

    @Serializable
    sealed interface Destination {
        val label: String

        @Serializable
        data object Photo : Destination {
            override val label: String = "Фото"
        }

        @Serializable
        data object Video : Destination {
            override val label: String = "Видео"
        }

        @Serializable
        data object Library : Destination {
            override val label: String = "Галерея"
        }
    }
}

@Composable
private fun PermissionRequestScreen(
    modifier: Modifier = Modifier,
    onRequestClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Для работы камеры требуются разрешения",
            textAlign = TextAlign.Center
        )
        Button(onClick = onRequestClick) {
            Text("Запросить разрешения")
        }
    }
}
