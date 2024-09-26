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

}
sealed class RecordingState {
    object Stopped : RecordingState()
    data class Started(val recording: Recording?) : RecordingState()
}
