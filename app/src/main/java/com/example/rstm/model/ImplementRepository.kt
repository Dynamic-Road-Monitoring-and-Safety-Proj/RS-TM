import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.Consumer

class ImplementRepository {

    private val _uriList = MutableLiveData<List<Uri>>(emptyList())
    val uriList: LiveData<List<Uri>> get() = _uriList

    // Helper to update _uriList safely
    private fun updateUriList(newList: List<Uri>) {
        _uriList.value = newList
    }

    // Find the URI of the video file by name
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

    // Initialize the URI list by checking for existing video files
    fun initializeUriList(context: Context) {
        val initialList = mutableListOf<Uri>()

        for (i in 0..5) {
            val fileName = "$i.mp4"
            val fileUri = findVideoUriByName(context, fileName)
            if (fileUri != null) {
                initialList.add(fileUri)
                Log.d("InitializeUriList", "Added existing video: $fileName")
            }
        }

        updateUriList(initialList)  // Update LiveData with the initialized list
        Log.d("InitializeUriList", "URI list initialized with ${initialList.size} items.")
    }

    // Delete the oldest video from the list
    private fun deleteOldestVideo(context: Context) {
        val currentList = _uriList.value?.toMutableList() ?: mutableListOf()
        if (currentList.isNotEmpty()) {
            val oldestUri = currentList[0]
            val deleted = context.contentResolver.delete(oldestUri, null, null)
            if (deleted > 0) {
                Log.d("DeleteVideo", "Deleted oldest video: $oldestUri")
                currentList.removeAt(0)
                updateUriList(currentList)
            } else {
                Log.e("DeleteVideo", "Failed to delete oldest video: $oldestUri")
            }
        }
    }

    // Rename the videos sequentially after deleting the oldest one
    private fun renameVideos(context: Context) {
        val currentList = _uriList.value?.toMutableList() ?: mutableListOf()
        for (i in currentList.indices) {
            val oldUri = currentList[i]
            val newName = "$i.mp4"
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, newName)
            }

            try {
                context.contentResolver.update(oldUri, contentValues, null, null)
                Log.d("RenameVideos", "Renamed file: $oldUri to $newName")
            } catch (e: Exception) {
                Log.e("RenameVideos", "Error renaming video: $oldUri", e)
            }
        }
        updateUriList(currentList)  // Update LiveData after renaming
    }

    // Capture video and manage circular buffer
    @SuppressLint("MissingPermission")
    fun captureVideo(videoCapture: VideoCapture<Recorder>, context: Context): Pair<PendingRecording, Consumer<VideoRecordEvent>> {
        val currentList = _uriList.value?.toMutableList() ?: mutableListOf()
        val name: String

        if (currentList.size >= 6) {
            deleteOldestVideo(context)  // Delete oldest video
            renameVideos(context)  // Rename remaining videos
            name = "5.mp4"  // Name the new video "5.mp4"
        } else {
            name = "${currentList.size}.mp4"  // Name new video sequentially
        }

        // Delete any existing file with the same name
        val existingUri = findVideoUriByName(context, name)
        if (existingUri != null) {
            context.contentResolver.delete(existingUri, null, null)
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
                        Log.d("CameraScreen", "Video recording succeeded: ${event.outputResults.outputUri}")
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "URI is ${event.outputResults.outputUri}", Toast.LENGTH_LONG).show()
                        }

                        val videoUri = event.outputResults.outputUri
                        currentList.add(videoUri)  // Add new URI to the list
                        updateUriList(currentList)  // Update LiveData with new list
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
}
