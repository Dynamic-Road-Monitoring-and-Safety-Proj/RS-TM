import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.rstm.MainActivity
import com.example.rstm.model.SensorData
import com.example.rstm.model.State
import com.example.rstm.roomImplementation.RoomEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class ImplementRepository() {

    private val state = MutableLiveData(State())

    private val _uriList = MutableLiveData<List<Uri>>(emptyList())
    private val CSVUriList = mutableListOf<Uri>()

    private val scope = CoroutineScope(Dispatchers.IO)
    val sensorDataList : MutableList<SensorData> = mutableListOf()

    fun listMaker(sensorData: SensorData){
        scope.launch(Dispatchers.IO) {
            sensorDataList.add(sensorData)
        }
    }

    val dao = MainActivity.appDatabase.getDao()
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


    fun saveSensorDataAsCSV(context: Context) {
        val csvFileName = "sensor_data.csv"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName)

        try {
            // Create the CSV string
            val csvBuilder = StringBuilder()
            csvBuilder.append("Timestamp,AccelerometerX,AccelerometerY,AccelerometerZ,MagneticX,MagneticY,MagneticZ,GyroscopeX,GyroscopeY,GyroscopeZ,Light,LocationLatitude,LocationLongitude\n") // Add header

            sensorDataList.forEach { sensorData ->

                csvBuilder.append(
                            "${sensorData.timestamp}," +
                            "${sensorData.accelerometerData.first}," +
                            "${sensorData.accelerometerData.second}," +
                            "${sensorData.accelerometerData.third}," +
                            "${sensorData.magneticData.first}," +
                            "${sensorData.magneticData.second}," +
                            "${sensorData.magneticData.third}," +
                            "${sensorData.gyroscopeData.first}," +
                            "${sensorData.gyroscopeData.second}," +
                            "${sensorData.gyroscopeData.third}," +
                            "${sensorData.lightData}," +
                            "${sensorData.locationData.latitude}," +
                            "${sensorData.locationData.longitude}\n"
                )
            }

            // Write the CSV string to the file
            FileOutputStream(filePath).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(csvBuilder.toString())
                }
            }

            // Get the URI of the saved file
            val savedUri = Uri.fromFile(filePath)
            Log.d("SaveSensorDataAsCSV", "CSV saved at: $savedUri")

            // Update your LiveData with the new URI
            state.value?.csvUri = savedUri // Update the csvUri property
            state.postValue(state.value) // Update LiveData with the new state

        } catch (e: Exception) {
            Log.e("SaveSensorDataAsCSV", "Error saving CSV: ${e.message}", e)
        }
    }


    fun saveToDatabase() {
        val roomEntity = RoomEntity(
            id = 0,
            videoUriList = state.value?.videoUriList,
            csvUri = state.value?.csvUri
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SaveToDatabase", "Inserting: $roomEntity")
                dao.insert(roomEntity)
                Log.d("SaveToDatabase", "Data saved successfully")
            } catch (e: Exception) {
                Log.e("SaveToDatabase", "Error saving data: ${e.message}", e)
            }
        }
    }
}