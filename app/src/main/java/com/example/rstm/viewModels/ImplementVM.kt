package com.example.rstm.viewModels

import android.content.Context
import android.hardware.SensorManager
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.video.Recording
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch

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

    private fun startRecording(context: Context) {
        // Use Model/Repository to capture video
        val videoCapture = getVideoCapture()
        val recording = videoCapture?.let {
            // Start recording logic
        }
        _recordingState.value = RecordingState.Started(recording)
    }

    private fun stopRecording() {
        currentRecording?.stop()
        _recordingState.value = RecordingState.Stopped
    }

    fun startCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, _lensFacing.value!!, getVideoCapture())
        }
    }
}
