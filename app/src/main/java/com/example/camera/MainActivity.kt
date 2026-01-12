package com.example.camera

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.camera.navigation.NavGraph
import com.example.camera.navigation.Screen
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

        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
        
        enableEdgeToEdge()
        setContent {
            CameraXAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val view = LocalView.current
                
                LaunchedEffect(Unit) {
                    if (!hasPermissions && !baseContext.hasAllPermissions()) {
                        requestPermissions()
                    }
                }
                
                LaunchedEffect(currentRoute) {
                    val isGallery = currentRoute == Screen.Library.route
                    val activityWindow = (view.context as? ComponentActivity)?.window
                    
                    activityWindow?.let { window ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (isGallery) {
                                window.insetsController?.show(WindowInsetsCompat.Type.statusBars())
                            } else {
                                window.insetsController?.hide(WindowInsetsCompat.Type.statusBars())
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            if (isGallery) {
                                window.decorView.systemUiVisibility = (
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                )
                            } else {
                                window.decorView.systemUiVisibility = (
                                    View.SYSTEM_UI_FLAG_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!hasPermissions) {
                        PermissionRequestScreen(
                            modifier = Modifier.fillMaxSize(),
                            onRequestClick = { requestPermissions() }
                        )
                    } else {
                        NavGraph(
                            navController = navController,
                            photoViewModel = imageViewModel,
                            videoViewModel = videoViewModel,
                            galleryViewModel = libraryViewModel,
                            modifier = Modifier.fillMaxSize()
                        )

                        val currentIndex = when (currentRoute) {
                            Screen.Photo.route -> 0
                            Screen.Video.route -> 1
                            else -> 0
                        }

                        if (currentRoute != Screen.Library.route) {
                            val navItems = listOf(
                                NavigationItem("Фото"),
                                NavigationItem("Видео")
                            )

                            BottomNavigationBar(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                selectedIndex = currentIndex,
                                items = navItems,
                                onItemSelected = { index ->
                                    val destination = when (index) {
                                        0 -> Screen.Photo.route
                                        1 -> Screen.Video.route
                                        else -> Screen.Photo.route
                                    }
                                    navController.navigate(destination) {
                                        popUpTo(Screen.Photo.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )

                            GalleryButton(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .navigationBarsPadding()
                                    .padding(bottom = 180.dp, end = 24.dp),
                                onClick = {
                                    navController.navigate(Screen.Library.route) {
                                        popUpTo(Screen.Photo.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(Permissions.required)
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
