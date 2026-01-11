package com.example.camera.fragment

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.ui.components.CameraSwitch
import com.example.camera.ui.components.GlassButton
import com.example.camera.utils.toRoundedOffset
import com.example.camera.viewmodel.PhotoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun PhotoFragment(
    viewModel: PhotoViewModel,
    modifier: Modifier = Modifier,
    paddings: PaddingValues = PaddingValues.Zero,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val cameraPreview by viewModel.surfaceRequest.collectAsStateWithLifecycle()

    var zoomLevel by remember { mutableStateOf(0f) }
    var isFlashing by remember { mutableStateOf(false) }
    var activeLens by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var focusPoint by remember { mutableStateOf(UUID.randomUUID() to Offset.Unspecified) }
    val focusPosition = remember(focusPoint.first) { focusPoint.second }

    LaunchedEffect(lifecycleOwner, activeLens) {
        viewModel.bindToCamera(
            appContext = context.applicationContext,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = activeLens
        )
    }

    if (focusPoint.second.isSpecified) {
        LaunchedEffect(focusPoint.first) {
            delay(1000)
            focusPoint = focusPoint.first to Offset.Unspecified
        }
    }

    Box(modifier = modifier) {
        cameraPreview?.let { preview ->
            val transformer = remember { MutableCoordinateTransformer() }

            CameraXViewfinder(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapPosition ->
                            with(transformer) {
                                viewModel.tapToFocus(tapPosition.transform())
                            }
                            focusPoint = UUID.randomUUID() to tapPosition
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, gestureZoom, _ ->
                            val newZoom = zoomLevel - (1f - gestureZoom)
                            zoomLevel = newZoom.coerceIn(0f, 1f)
                            viewModel.changeZoom(zoomLevel)
                        }
                    },
                surfaceRequest = preview
            )

            AnimatedVisibility(
                modifier = Modifier
                    .offset { focusPosition.toRoundedOffset() }
                    .offset(-24.dp, -24.dp),
                visible = focusPoint.second.isSpecified,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Canvas(modifier = Modifier.size(48.dp)) {
                    val border = 2.dp.toPx()
                    drawCircle(
                        radius = (size.minDimension - border) / 2,
                        color = Color.White,
                        style = Stroke(width = border)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isFlashing,
            enter = fadeIn(tween(50)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        GlassButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            isActive = false,
            onClick = {
                viewModel.takePhoto(context)
                scope.launch {
                    isFlashing = true
                    delay(50)
                    isFlashing = false
                }
            }
        )

        CameraSwitch(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp),
            onClick = {
                activeLens = if (activeLens == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            }
        )
    }
}
