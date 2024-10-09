package com.example.rstm.viewModels

import ImplementRepository
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.hardware.SensorManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ImplementVM(
    val sensorManager: SensorManager,
    val fusedLocationClient: FusedLocationProviderClient, // remove these two from UI
    val implementRepo : ImplementRepository
) : ViewModel() {
    // State management
    private val _lensFacing = MutableLiveData(CameraSelector.LENS_FACING_BACK)
    val lensFacing = _lensFacing
    private val _recordingState = MutableLiveData<RecordingState>()
    val recordingState: LiveData<RecordingState> = _recordingState

    private var currentRecording: Recording? = null

    // Toggle between front and back camera
    fun toggleLensFacing() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
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

    fun fetchCameraProvider(context: Context,lifecycleOwner : LifecycleOwner, cameraxSelector:CameraSelector, videoCapture: VideoCapture<Recorder>) {
        viewModelScope.launch {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, videoCapture)

        }
    }

    private fun findVideoUriByName(context: Context, fileName: String): Uri? {
        val projection = arrayOf(MediaStore.Video.Media._ID)
        val selection = "${MediaStore.Video.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        context.contentResolver.query(queryUri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                return Uri.withAppendedPath(queryUri, id.toString())
            }
        }
        return null
    }

    // support functions for capture video function
    private fun deleteOldestVideo(context: Context, uriList: MutableList<Uri>) {
        if (uriList.isNotEmpty()) {
            val oldestUri = uriList[0]
            val deleted = context.contentResolver.delete(oldestUri, null, null)
            if (deleted > 0) {
                Log.d("DeleteVideo", "Deleted oldest video: $oldestUri")
                uriList.removeAt(0)
            } else {
                Log.e("DeleteVideo", "Failed to delete oldest video: $oldestUri")
            }
        }
    }
    private fun renameVideos(context: Context, uriList: MutableList<Uri>) {
        for (i in uriList.indices) {
            val oldUri = uriList[i]
            val newName = "$i.mp4"
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, newName)
            }

            try {
                // Ensure the file exists before renaming
                context.contentResolver.openInputStream(oldUri)?.close() ?: run {
                    Log.e("RenameVideos", "File does not exist, skipping rename: $oldUri")
                }

                context.contentResolver.update(oldUri, contentValues, null, null)
                Log.d("RenameVideos", "Renamed file: $oldUri to $newName")
            } catch (e: Exception) {
                Log.e("RenameVideos", "Error renaming video: $oldUri", e)
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun captureVideo(
        videoCapture: VideoCapture<Recorder>,
        context: Context
    ): Pair<PendingRecording, androidx.core.util.Consumer<VideoRecordEvent>> {

        var name: String = "0.mp4"

        val uriList : MutableList<Uri>? = implementRepo.getUriList()

        uriList?.size?.let {
            if (it >= 6) {
                // Circular buffer: when there are 6 videos, delete the oldest and rename the others
                deleteOldestVideo(context, uriList )
                renameVideos(context, uriList)  // Renames from 0.mp4 to 4.mp4
                implementRepo.updateUriList(uriList)
                name = "5.mp4"  // New video will be named "5.mp4"
            } else {
                // If the list size is less than 6, name videos sequentially from "0.mp4" to "4.mp4"
                name = "${uriList.size}.mp4"
            }
        }

        val existingUri = findVideoUriByName(context, name)
        if (existingUri != null) {
            context.contentResolver.delete(existingUri, null, null)  // Delete existing file
            Log.d("CameraScreen", "Deleted existing video: $name")
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        val captureListener = Consumer<VideoRecordEvent> { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    Log.d("CameraScreen", "Recording started")
                }

                is VideoRecordEvent.Finalize -> {
                    if (event.error == VideoRecordEvent.Finalize.ERROR_NONE) {
                        Log.d(
                            "CameraScreen",
                            "Video recording succeeded: ${event.outputResults.outputUri}"
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                "URI is ${event.outputResults.outputUri}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        val videoUri = event.outputResults.outputUri
                        // Now add this new video to the list
                        implementRepo.addUri(videoUri)
                    } else {
                        Log.e("CameraScreen", "Video recording failed: ${event.cause}")
                    }
                }

                else -> {
                    // Handle other events if needed
                }
            }
        }

        val recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutput)
            .withAudioEnabled()

        return Pair(recording, captureListener)
    }
    fun getSensorManager() : SensorManager{
        return sensorManager
    }
    fun getFusedLocation() : FusedLocationProviderClient{
        return fusedLocationClient
    }
}

sealed class RecordingState {
    object Stopped : RecordingState()
    data class Started(val recording: Recording?) : RecordingState()
}