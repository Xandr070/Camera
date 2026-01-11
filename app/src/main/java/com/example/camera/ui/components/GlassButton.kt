package com.example.camera.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GlassButton(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = Color.Red,
    onClick: () -> Unit
) {
    val animation = remember { Animatable(if (isActive) 1f else 0f) }
    
    LaunchedEffect(isActive) {
        animation.animateTo(if (isActive) 1f else 0f, tween(200))
    }
    
    Canvas(
        modifier = modifier
            .size(56.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = null
            )
    ) {
        val borderWidth = 2.dp.toPx()
        val innerOffset = 4.dp.toPx()
        val maxOffset = ((size.minDimension / 2) - borderWidth) / 3 * 2
        val currentOffset = innerOffset + (maxOffset - innerOffset) * animation.value
        
        drawCircle(
            radius = size.minDimension / 2 - borderWidth,
            color = Color.White,
            style = Stroke(width = borderWidth)
        )
        
        drawCircle(
            radius = size.minDimension / 2 - borderWidth - currentOffset,
            color = if (isActive) activeColor else Color.White
        )
    }
}

