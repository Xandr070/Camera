package com.example.camera.navigation
sealed class Screen(val route: String) {
    data object Photo : Screen("photo")
    data object Video : Screen("video")
    data object Library : Screen("library")
}
