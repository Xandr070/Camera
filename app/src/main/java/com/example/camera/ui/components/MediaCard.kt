package com.example.camera.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MediaCard(
    modifier: Modifier = Modifier,
    imageUri: String,
    dateText: String,
    onClick: () -> Unit
) {
    Box {
        AsyncImage(
            modifier = modifier
                .aspectRatio(0.75f)
                .clickable(onClick = onClick),
            model = imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Text(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(4.dp),
            text = dateText
        )
    }
}

