package com.example.rstm.viewModels

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.SensorManager
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ImplementVM(
    private val sensorManager: SensorManager,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    // State management
    private val _lensFacing = MutableLiveData(CameraSelector.LENS_FACING_BACK)
    val lensFacing = _lensFacing

    private val _recordingState = MutableLiveData<RecordingState>()
    val recordingState: LiveData<RecordingState> = _recordingState

    private val _uriList = MutableLiveData<List<Uri>>(emptyList())
    val uriList: LiveData<List<Uri>> = _uriList

    private var currentRecording: Recording? = null

    // Toggle between front and back camera
    fun toggleLensFacing() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    // Start/stop recording
    fun toggleRecording(context: Context) {
        if (currentRecording != null) {
            stopRecording()
        } else {
            startRecording(context)
        }
    }
    private var videoCapture: VideoCapture<Recorder>? = null

    fun getVideoCapture(): VideoCapture<Recorder>? {
        if (videoCapture == null) {
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)
        }
        return videoCapture
    }

    @SuppressLint("MissingPermission")
    private fun startRecording(context: Context) {
        // Get or initialize the VideoCapture instance
        val videoCapture = getVideoCapture()

        videoCapture?.let {
            // Define the output file path for the video
            val videoFile = File(context.filesDir, "video_${System.currentTimeMillis()}.mp4")

            // Set up output options for the video file
            val outputOptions = FileOutputOptions.Builder(videoFile).build()

            // Prepare the recording
            currentRecording = it.output
                .prepareRecording(context, outputOptions)
                .withAudioEnabled() // Enables audio recording
                .start(ContextCompat.getMainExecutor(context)) { videoRecordEvent ->
                    when (videoRecordEvent) {
                        is VideoRecordEvent.Start -> {
                            // Recording has started
                            _recordingState.value = RecordingState.Started(currentRecording)
                        }
                        is VideoRecordEvent.Finalize -> {
                            // Recording has stopped or finalized
                            _recordingState.value = RecordingState.Stopped
                            // Handle finalization (e.g., save the file path or notify the user)
                        }
                    }
                }
        }
    }




    fun stopRecording() {
        currentRecording?.stop()
        _recordingState.value = RecordingState.Stopped
    }

    fun startCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            val cameraxSelector =
                _lensFacing.value?.let { CameraSelector.Builder().requireLensFacing(it).build() }
            if (cameraxSelector != null) {
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, getVideoCapture())
            }
        }
    }
    private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(this).also { cameraProvider ->
                cameraProvider.addListener({
                    continuation.resume(cameraProvider.get())
                }, ContextCompat.getMainExecutor(this))
            }
        }
}
sealed class RecordingState {
    object Stopped : RecordingState()
    data class Started(val recording: Recording?) : RecordingState()
}
