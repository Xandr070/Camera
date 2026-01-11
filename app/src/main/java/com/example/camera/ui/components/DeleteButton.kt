package com.example.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import com.example.camera.ui.theme.icons.CameraIcons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DeleteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isOnWhiteBackground: Boolean = false
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = if (isOnWhiteBackground) {
                        listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.05f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    }
                ),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = if (isOnWhiteBackground) {
                    Color.Black.copy(alpha = 0.2f)
                } else {
                    Color.White.copy(alpha = 0.3f)
                },
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CameraIcons.Delete,
            contentDescription = null,
            tint = if (isOnWhiteBackground) Color.Black else Color.White
        )
    }
}



