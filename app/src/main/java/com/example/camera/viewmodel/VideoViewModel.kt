package com.example.camera.viewmodel

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VideoViewModel : ViewModel() {

    private var focusFactory: SurfaceOrientedMeteringPointFactory? = null
    private var control: CameraControl? = null
    private var activeRecording: Recording? = null

    private val preview = Preview.Builder().build().apply {
        setSurfaceProvider { request ->
            _cameraPreview.update { request }
            focusFactory = SurfaceOrientedMeteringPointFactory(
                request.resolution.width.toFloat(),
                request.resolution.height.toFloat()
            )
        }
    }

    private val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())

    private val _cameraPreview = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _cameraPreview.asStateFlow()

    private val _recordingState = MutableStateFlow<VideoRecordEvent?>(null)
    val videoRecord: StateFlow<VideoRecordEvent?> = _recordingState.asStateFlow()

    suspend fun bindToCamera(
        appContext: Context,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) {
        val wasRecording = activeRecording?.isPersistent ?: false
        if (wasRecording) activeRecording?.pause()

        val provider = ProcessCameraProvider.awaitInstance(appContext)
        provider.unbindAll()
        val camera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            videoCapture
        )

        control = camera.cameraControl

        if (wasRecording) activeRecording?.resume()

        try {
            awaitCancellation()
        } finally {
            control = null
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun captureVideo(context: Context) {
        activeRecording?.run {
            stop()
            activeRecording = null
            return
        }

        val fileName = "VID_${System.currentTimeMillis()}"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Movies/CameraX-App")
        }

        val options = MediaStoreOutputOptions
            .Builder(
                context.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            .setContentValues(values)
            .build()

        activeRecording = videoCapture.output
            .prepareRecording(context, options)
            .asPersistentRecording()
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                _recordingState.value = event

                if (event is VideoRecordEvent.Finalize) {
                    if (!event.hasError()) {
                        Toast.makeText(
                            context,
                            "Видео сохранено: ${event.outputResults.outputUri}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        activeRecording?.close()
                        activeRecording = null
                        Toast.makeText(
                            context,
                            "Ошибка: ${event.error}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    fun changeZoom(level: Float) {
        control?.setLinearZoom(level)
    }
}
