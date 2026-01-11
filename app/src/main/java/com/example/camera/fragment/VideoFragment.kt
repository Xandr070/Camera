package com.example.camera.fragment

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.camera.viewmodel.VideoViewModel
import androidx.compose.material.icons.filled.ArrowForward

@Composable
fun VideoFragment(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier,
    paddings: PaddingValues = PaddingValues.Zero,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val record by viewModel.videoRecord.collectAsStateWithLifecycle()

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var zoom by remember { mutableStateOf(0f) }

    LaunchedEffect(lifecycleOwner, cameraSelector) {
        viewModel.bindToCamera(
            appContext = context.applicationContext,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector,
        )
    }

    Box(modifier = modifier) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, gestureZoom, _ ->
                            val newScale = zoom - (1f - gestureZoom)

                            zoom = newScale.coerceIn(0f, 1f)
                            viewModel.changeZoom(zoom)
                        }
                    },
                surfaceRequest = request,
            )
        }

        val animationBorder =
            remember { Animatable(if (record == null || record is Finalize) 0f else 1f) }
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(paddings)
                .size(56.dp)
                .clickable(
                    onClick = {
                        viewModel.captureVideo(context)

                        coroutineScope.launch {
                            val animateTo = if (record == null || record is Finalize) 1f else 0f

                            if (animationBorder.isRunning) animationBorder.snapTo(1f - animateTo)
                            animationBorder.animateTo(animateTo)
                        }
                    },
                    indication = null,
                    interactionSource = null,
                )
        ) {
            val borderSize = 2.dp.toPx()
            val minOffset = 4.dp.toPx()
            val maxOffset = ((size.minDimension / 2) - borderSize) / 3 * 2

            drawCircle(
                color = Color.White,
                style = Stroke(width = borderSize),
                radius = size.minDimension / 2 - borderSize,
            )
            drawCircle(
                color = Color.Red,
                radius = size.minDimension / 2 - borderSize - (minOffset + (maxOffset - minOffset) * animationBorder.value),
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(paddings)
                .size(56.dp),
            onClick = {
                cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
            )
        }

        val transition = rememberInfiniteTransition()
        val animation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = InfiniteRepeatableSpec(tween(500), repeatMode = RepeatMode.Reverse),
        )
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.TopCenter),
            visible = record != null && record !is Finalize,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = animation }
                        .background(Color.Red, shape = CircleShape)
                )
                Text(
                    text = "%.2f".format(record?.run { recordingStats.recordedDurationNanos / 1_000_000 / 1000f })
                )
            }
        }
    }
}
