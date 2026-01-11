package com.example.camera.viewmodel

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
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

    private var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    private var cameraControl: CameraControl? = null
    private var recording: Recording? = null

    private val previewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
            surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat(),
            )
        }
    }

    private val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    private val _videoRecordEvent = MutableStateFlow<VideoRecordEvent?>(null)
    val videoRecord: StateFlow<VideoRecordEvent?> = _videoRecordEvent.asStateFlow()

    suspend fun bindToCamera(
        appContext: Context,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    ) {
        val isRecorded = recording?.isPersistent ?: false
        if (isRecorded) recording?.pause()

        val cameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, previewUseCase, videoCapture
        )

        cameraControl = camera.cameraControl

        if (isRecorded) recording?.resume()

        try {
            awaitCancellation()
        } finally {
            cameraControl = null
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun captureVideo(context: Context) {
        recording?.run {
            stop()

            recording = null
            return
        }

        val fileName = "Video_${System.currentTimeMillis()}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Movies/CameraX-App")
        }

        val outputOptions = MediaStoreOutputOptions
            .Builder(
                context.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            )
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .asPersistentRecording()
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                _videoRecordEvent.value = recordEvent

                if (recordEvent is VideoRecordEvent.Finalize) {
                    if (!recordEvent.hasError()) {
                        val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"

                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    } else {
                        recording?.close()
                        recording = null

                        Toast.makeText(
                            context, "Video capture ends with error: ${recordEvent.error}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
    }

    fun changeZoom(linear: Float) {
        cameraControl?.setLinearZoom(linear)
    }

    companion object {
        private const val TAG = "VideoCaptureViewModel"
    }
}
