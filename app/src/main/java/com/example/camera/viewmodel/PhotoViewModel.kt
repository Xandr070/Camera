package com.example.camera.viewmodel

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PhotoViewModel : ViewModel() {

    private var focusFactory: SurfaceOrientedMeteringPointFactory? = null
    private var control: CameraControl? = null

    private val preview = Preview.Builder().build().apply {
        setSurfaceProvider { request ->
            _cameraPreview.update { request }
            focusFactory = SurfaceOrientedMeteringPointFactory(
                request.resolution.width.toFloat(),
                request.resolution.height.toFloat()
            )
        }
    }

    private val capture = ImageCapture.Builder().build()

    private val _cameraPreview = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _cameraPreview.asStateFlow()
    
    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_OFF)
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    suspend fun bindToCamera(
        appContext: Context,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) {
        val provider = ProcessCameraProvider.awaitInstance(appContext)
        provider.unbindAll()
        val camera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            capture
        )

        control = camera.cameraControl
        capture.flashMode = _flashMode.value

        try {
            awaitCancellation()
        } finally {
            control = null
        }
    }

    fun takePhoto(context: Context) {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-App")
        }

        val options = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            .build()

        capture.takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        context,
                        "Фото сохранено: ${output.savedUri}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        context,
                        "Ошибка: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    fun tapToFocus(position: Offset) {
        focusFactory?.createPoint(position.x, position.y)?.let { point ->
            val action = FocusMeteringAction.Builder(point).build()
            control?.startFocusAndMetering(action)
        }
    }

    fun changeZoom(level: Float) {
        control?.setLinearZoom(level)
    }
    
    fun toggleFlash() {
        val newMode = when (_flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
            else -> ImageCapture.FLASH_MODE_OFF
        }
        _flashMode.value = newMode
        capture.flashMode = newMode
    }
    
    fun setFlashMode(mode: Int) {
        _flashMode.value = mode
        capture.flashMode = mode
    }
}
