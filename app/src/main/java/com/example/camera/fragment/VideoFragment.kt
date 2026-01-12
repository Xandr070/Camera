package com.example.camera.fragment

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.camera.ui.theme.CameraXAppTheme
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.ui.components.CameraSwitch
import com.example.camera.ui.components.GlassButton
import com.example.camera.viewmodel.VideoViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Composable
fun VideoFragment(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cameraPreview by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val recordingState by viewModel.videoRecord.collectAsStateWithLifecycle()

    var activeLens by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var zoomLevel by remember { mutableStateOf(0f) }
    val isRecording = recordingState != null && recordingState !is Finalize

    LaunchedEffect(lifecycleOwner, activeLens) {
        viewModel.bindToCamera(
            appContext = context.applicationContext,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = activeLens
        )
    }

    Box(modifier = modifier) {
        cameraPreview?.let { preview ->
            CameraXViewfinder(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, gestureZoom, _ ->
                            val newZoom = zoomLevel - (1f - gestureZoom)
                            zoomLevel = newZoom.coerceIn(0f, 1f)
                            viewModel.changeZoom(zoomLevel)
                        }
                    },
                surfaceRequest = preview
            )
        }

        GlassButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            isActive = isRecording,
            activeColor = Color.Red,
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    @SuppressLint("MissingPermission")
                    viewModel.captureVideo(context)
                }
            }
        )

        CameraSwitch(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 100.dp, end = 24.dp),
            onClick = {
                activeLens = if (activeLens == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            }
        )

        val pulseAnimation = rememberInfiniteTransition()
        val pulseAlpha by pulseAnimation.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = InfiniteRepeatableSpec(
                tween(500),
                repeatMode = RepeatMode.Reverse
            )
        )

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 180.dp),
            visible = isRecording,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = pulseAlpha }
                        .background(Color.Red, shape = CircleShape)
                )
                Text(
                    text = "%.2f".format(
                        recordingState?.run {
                            recordingStats.recordedDurationNanos / 1_000_000 / 1000f
                        } ?: 0f
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Video Fragment")
@Composable
private fun VideoFragmentPreview() {
    CameraXAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
            
            GlassButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                isActive = false,
                activeColor = Color.Red,
                onClick = {}
            )

            CameraSwitch(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 24.dp),
                onClick = {}
            )
        }
    }
}
