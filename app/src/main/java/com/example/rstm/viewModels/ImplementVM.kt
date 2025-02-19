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
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ImplementVM(
    private val sensorManager: SensorManager,
    private val fusedLocationClient: FusedLocationProviderClient, // remove these two from UI
    private val implementRepo : ImplementRepository
) : ViewModel() {
    // State management
    private val _lensFacing = MutableLiveData(CameraSelector.LENS_FACING_BACK)

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
    private fun deleteOldestVideo(context: Context, uriList: MutableList<Uri>, maxVideos: Int = 4): Int {
        if (uriList.size > maxVideos) {  // Keep at least `maxVideos` videos
            val oldestUri = uriList[0]
            val deleted = context.contentResolver.delete(oldestUri, null, null)

            if (deleted > 0) {
                Log.d("DeleteVideo", "Deleted oldest video: $oldestUri")
                uriList.removeAt(0)
                return 1
            } else {
                Log.e("DeleteVideo", "Failed to delete oldest video: $oldestUri")
                return 0
            }
        }
        return 0  // No deletion needed
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
    ): Pair<PendingRecording, Consumer<VideoRecordEvent>> {

        val bufferSize = 5  // Keep last 5 videos
        val uriList: MutableList<Uri>? = implementRepo.getUriList()

        // Delete oldest clip if buffer is full
        if (uriList != null && uriList.size >= bufferSize) {
            val oldestUri = uriList.removeAt(0)  // Remove first (oldest) entry
            context.contentResolver.delete(oldestUri, null, null)
            Log.d("CameraScreen", "Deleted oldest video: $oldestUri")
        }

        // Rename existing videos to shift them down (4.mp4 → 3.mp4, etc.)
        if (uriList != null) {
            renameVideos(context, uriList)
        }

        // Newest clip should always be saved as "5.mp4"
        val fileName = "5.mp4"

        // Delete existing 5.mp4 (to avoid duplicates)
        val existingUri = findVideoUriByName(context, fileName)
        if (existingUri != null) {
            context.contentResolver.delete(existingUri, null, null)
            Log.d("CameraScreen", "Deleted existing video: $fileName")
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
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
                        val videoUri = event.outputResults.outputUri
                        implementRepo.addUri(videoUri)  // Store new video URI
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
    fun getRepository() :ImplementRepository{
        return implementRepo
    }
}