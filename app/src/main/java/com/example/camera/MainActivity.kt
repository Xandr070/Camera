package com.example.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.camera.fragment.GalleryFragment
import com.example.camera.fragment.PhotoFragment
import com.example.camera.fragment.VideoFragment
import com.example.camera.ui.theme.CameraXAppTheme
import com.example.camera.viewmodel.GalleryViewModel
import com.example.camera.viewmodel.PhotoViewModel
import com.example.camera.viewmodel.VideoViewModel
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "Permission result: $permissions")
        
        val permissionGranted = permissions.entries.asSequence()
            .filter { it.key in REQUIRED_PERMISSIONS }
            .all { it.value }

        if (!permissionGranted) {
            val deniedPermissions = permissions.entries
                .filter { it.key in REQUIRED_PERMISSIONS && !it.value }
                .joinToString(", ") { it.key }

            Log.w(TAG, "Permissions denied: $deniedPermissions")
            
            Toast.makeText(
                baseContext,
                "Permission request denied: $deniedPermissions. Please grant permissions in Settings.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Log.d(TAG, "All permissions granted")
            isCameraGranted = true
        }
    }

    private var isCameraGranted by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: checking permissions")
        if (allPermissionsGranted()) {
            Log.d(TAG, "All permissions already granted")
            isCameraGranted = true
        } else {
            Log.d(TAG, "Permissions not granted, will request on first UI render")
        }

        val galleryViewModel: GalleryViewModel by viewModels()
        val photoViewModel: PhotoViewModel by viewModels()
        val videoViewModel: VideoViewModel by viewModels()

        val destinations = listOf(Screen.Image, Screen.Video, Screen.Gallery)

        enableEdgeToEdge()
        setContent {
            val backStack = remember { mutableStateListOf<Screen>(Screen.Image) }

            var selectedIndex by remember { mutableIntStateOf(0) }

            CameraXAppTheme {
                LaunchedEffect(Unit) {
                    if (!isCameraGranted && !allPermissionsGranted()) {
                        Log.d(TAG, "Auto-requesting permissions on first render")
                        requestPermissions()
                    }
                }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        PrimaryTabRow(
                            selectedTabIndex = selectedIndex, modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                        ) {
                            destinations.forEachIndexed { index, destination ->
                                Tab(
                                    selected = backStack.last() == destination,
                                    onClick = {
                                        backStack.clear()
                                        backStack.add(destination)
                                        selectedIndex = index
                                    },
                                    text = { Text(destination.name) }
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    if (!isCameraGranted) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Text(
                                text = "Need permissions for work with camera",
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { requestPermissions() }) {
                                Text("request")
                            }
                        }
                    } else {
                        NavDisplay(
                            modifier = Modifier
                                .fillMaxSize(),
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            entryProvider = entryProvider {
                                entry<Screen.Image> {
                                    PhotoFragment(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = innerPadding.calculateTopPadding()),
                                        paddings = innerPadding,
                                        viewModel = photoViewModel,
                                    )
                                }
                                entry<Screen.Video> {
                                    VideoFragment(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = innerPadding.calculateTopPadding()),
                                        paddings = innerPadding,
                                        viewModel = videoViewModel,
                                    )
                                }
                                entry<Screen.Gallery>(
                                    metadata = NavDisplay.transitionSpec {
                                        slideInHorizontally(
                                            initialOffsetX = { it },
                                            animationSpec = tween(1000)
                                        ) togetherWith
                                                slideOutHorizontally(
                                                    targetOffsetX = { -it },
                                                    animationSpec = tween(1000)
                                                )
                                    }
                                ) {
                                    GalleryFragment(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = innerPadding.calculateTopPadding()),
                                        paddings = innerPadding,
                                        viewModel = galleryViewModel,
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        Log.d(TAG, "Requesting permissions: ${REQUIRED_PERMISSIONS.joinToString()}")
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @Serializable
    sealed interface Screen {
        val name: String

        @Serializable
        data object Image : Screen {
            override val name: String = "Photo"
        }

        @Serializable
        data object Video : Screen {
            override val name: String = "Video"
        }

        @Serializable
        data object Gallery : Screen {
            override val name: String = "Gallery"
        }
    }

    companion object {
        private const val TAG = "CameraXApp"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}
