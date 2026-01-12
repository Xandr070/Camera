package com.example.camera.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.camera.fragment.GalleryFragment
import com.example.camera.fragment.PhotoFragment
import com.example.camera.fragment.VideoFragment
import com.example.camera.viewmodel.GalleryViewModel
import com.example.camera.viewmodel.PhotoViewModel
import com.example.camera.viewmodel.VideoViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    photoViewModel: PhotoViewModel,
    videoViewModel: VideoViewModel,
    galleryViewModel: GalleryViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Photo.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable(route = Screen.Photo.route) {
            PhotoFragment(
                modifier = Modifier.fillMaxSize(),
                viewModel = photoViewModel,
                lifecycleOwner = LocalLifecycleOwner.current
            )
        }

        composable(route = Screen.Video.route) {
            VideoFragment(
                modifier = Modifier.fillMaxSize(),
                viewModel = videoViewModel,
                lifecycleOwner = LocalLifecycleOwner.current
            )
        }

        composable(route = Screen.Library.route) {
            GalleryFragment(
                modifier = Modifier.fillMaxSize(),
                viewModel = galleryViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
