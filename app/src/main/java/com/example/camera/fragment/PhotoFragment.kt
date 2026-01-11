package com.example.camera.fragment

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.takeOrElse
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.camera.viewmodel.PhotoViewModel
import androidx.compose.material.icons.filled.ArrowForward
import java.util.UUID

@Composable
fun PhotoFragment(
    viewModel: PhotoViewModel,
    modifier: Modifier = Modifier,
    paddings: PaddingValues = PaddingValues.Zero,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tapCircleSize = 48.dp

    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()

    var zoom by remember { mutableStateOf(0f) }
    var flash by remember { mutableStateOf(false) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var focusRequest by remember { mutableStateOf(UUID.randomUUID() to Offset.Unspecified) }
    val focusCoordinates = remember(focusRequest.first) { focusRequest.second }

    if (focusRequest.second.isSpecified) {
        LaunchedEffect(focusRequest.first) {
            delay(1000)
            focusRequest = focusRequest.first to Offset.Unspecified
        }
    }

    LaunchedEffect(lifecycleOwner, cameraSelector) {
        viewModel.bindToCamera(
            appContext = context.applicationContext,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector,
        )
    }

    Box(modifier = modifier) {
        surfaceRequest?.let { request ->
            val coordinateTransformer = remember { MutableCoordinateTransformer() }

            CameraXViewfinder(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapCoordinates ->
                            with(coordinateTransformer) {
                                viewModel.tapToFocus(tapCoordinates.transform())
                            }
                            focusRequest = UUID.randomUUID() to tapCoordinates
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, gestureZoom, _ ->
                            val newScale = zoom - (1f - gestureZoom)

                            zoom = newScale.coerceIn(0f, 1f)
                            viewModel.changeZoom(zoom)
                        }
                    },
                surfaceRequest = request,
            )

            AnimatedVisibility(
                modifier = Modifier
                    .offset { focusCoordinates.takeOrElse { Offset.Zero }.round() }
                    .offset(-tapCircleSize / 2, -tapCircleSize / 2),
                visible = focusRequest.second.isSpecified,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Canvas(modifier = Modifier.size(tapCircleSize)) {
                    val borderSize = 2.dp.toPx()
                    drawCircle(
                        radius = (size.minDimension - borderSize) / 2,
                        color = Color.White,
                        style = Stroke(width = borderSize)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = flash,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        val animationBorder = remember { Animatable(0f) }
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(paddings)
                .size(56.dp)
                .clickable(
                    onClick = {
                        viewModel.takePhoto(context)
                        coroutineScope.launch {
                            if (animationBorder.isRunning) animationBorder.snapTo(0f)

                            flash = true
                            animationBorder.animateTo(1f)
                            delay(50)
                            flash = false
                            delay(150)
                            animationBorder.animateTo(0f)
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
                radius = size.minDimension / 2 - borderSize,
                color = Color.White,
                style = Stroke(width = borderSize)
            )
            drawCircle(
                radius = size.minDimension / 2 - borderSize - (minOffset + (maxOffset - minOffset) * animationBorder.value),
                color = Color.White
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
                contentDescription = null,
                imageVector = Icons.Filled.ArrowForward,
            )
        }
    }
}
