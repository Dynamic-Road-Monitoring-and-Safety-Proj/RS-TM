import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.RoomDatabase
import com.example.rstm.roomImplementation.RoomDao
import com.example.rstm.roomImplementation.RoomEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImplementRepository {

    private val _uriList = MutableLiveData<List<Uri>>(emptyList())
    val uriList: LiveData<List<Uri>> get() = _uriList

    // Helper to update _uriList safely
    fun updateUriList(newList: List<Uri>) {
        _uriList.postValue(newList)  // Use postValue for background thread
    }
    fun getUriList(): MutableList<Uri>? {
        return _uriList.value?.toMutableList() ?: mutableListOf()
    }
    fun addUri(newUri: Uri) {
        val currentList = _uriList.value?.toMutableList() ?: mutableListOf()  // Get current list or create a new one
        currentList.add(newUri)  // Add the new Uri
        updateUriList(currentList)  // Update LiveData with new list
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
    fun saveToDatabase(context: Context) {
        // Get the current list of URIs (convert them to Strings)
        val currentUriList = getUriList()?.map { it.toString() } ?: emptyList()

        // Create a RoomEntity object
        val roomEntity = RoomEntity(
            id = 0,  // Set to 0 for auto-generation
            videoUriList = currentUriList,  // Store the URI list
            accelerometerUri = null,  // Placeholder for sensor data
            gyroUri = null,
            locationUri = null,
            light_Uri = null,
            time_Uri = null
        )

        // Use coroutine scope to save to the database asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Insert the RoomEntity into the database
                dao.insert(roomEntity)
                Log.d("SaveToDatabase", "Data saved to the database.")
            } catch (e: Exception) {
                Log.e("SaveToDatabase", "Error saving data: ${e.message}")
            }
        }
    }

}