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
import java.sql.Timestamp

class ImplementRepository() {

    private val state = MutableLiveData(State())

    private val _uriList = MutableLiveData<List<Uri>>(emptyList())

    private val scope = CoroutineScope(Dispatchers.IO)
    val sensorDataList : MutableList<SensorData> = mutableListOf()

    fun listMaker(sensorData: SensorData) {
        scope.launch(Dispatchers.IO) {
            val newSensorData = sensorData.copy(
                timestamp = Timestamp(System.currentTimeMillis())
            )
            sensorDataList.add(newSensorData)
        }
    }

    val dao = MainActivity.appDatabase.getDao()
    // Helper to update _uriList safely
    // Helper function to log the current _uriList state

    private fun printUriList() {
        Log.d("ImplementRepository", "Current URI List: ${_uriList.value ?: emptyList<Uri>()}")
    }

    fun updateUriList(newList: List<Uri>) {
        _uriList.postValue(newList)  // Use postValue for background thread
        Log.d("ImplementRepository", "updateUriList called. New list size: ${newList.size}")
        printUriList()
    }

    fun getUriList(): MutableList<Uri>? {
        val currentList = _uriList.value?.toMutableList() ?: mutableListOf()
        Log.d("ImplementRepository", "getUriList called. Current list size: ${currentList.size}")
        printUriList()
        return currentList
    }

    fun addUri(newUri: Uri) {
        val currentList = _uriList.value?.toMutableList() ?: mutableListOf()  // Get current list or create a new one
        currentList.add(newUri)  // Add the new Uri
        updateUriList(currentList)  // Update LiveData with new list
        Log.d("ImplementRepository", "addUri called. Added URI: $newUri")
        printUriList()
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

    fun appendSensorDataToCSV(context: Context, sensorData: SensorData) {
        val csvFileName = "sensor_data.csv"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName)
        val maxFileSize = 100 * 1024 * 1024 // 100 MB in bytes
        val maxReadings = 40000

        try {
            // Create the file if it doesn't exist and add the header
            if (!filePath.exists()) {
                filePath.writeText(
                    "Timestamp,AccelerometerX,AccelerometerY,AccelerometerZ,GyroscopeX,GyroscopeY,GyroscopeZ,Light,LocationLatitude,LocationLongitude,Altitude,Speed\n"
                )
            }

            // Append the new data
            val csvLine = "${sensorData.timestamp}," +
                    "${sensorData.accelerometerData.first}," +
                    "${sensorData.accelerometerData.second}," +
                    "${sensorData.accelerometerData.third}," +
                    "${sensorData.gyroscopeData.first}," +
                    "${sensorData.gyroscopeData.second}," +
                    "${sensorData.gyroscopeData.third}," +
                    "${sensorData.lightData}," +
                    "${sensorData.locationData.latitude}," +
                    "${sensorData.locationData.longitude}," +
                    "${sensorData.locationData.altitude}," +
                    "${sensorData.locationData.speed}\n"

            filePath.appendText(csvLine)

            // Check file size and number of readings
            if (filePath.length() > maxFileSize || countReadings(filePath) > maxReadings) {
                trimCSVFile(filePath)
            }
        } catch (e: Exception) {
            Log.e("AppendSensorDataToCSV", "Error appending data: ${e.message}", e)
        }
    }

    // Function to count the number of readings in the CSV file (excluding the header)
    fun countReadings(file: File): Int {
        return try {
            file.readLines().size - 1 // Subtract 1 for the header
        } catch (e: Exception) {
            Log.e("CountReadings", "Error counting readings: ${e.message}", e)
            0
        }
    }

    // Function to trim the first 50% of data from the CSV file
    fun trimCSVFile(file: File) {
        try {
            val lines = file.readLines()
            val header = lines.first()
            val data = lines.drop(1) // Exclude the header
            val trimmedData = data.drop(data.size / 2) // Drop the first 50% of rows

            // Rewrite the file with the header and trimmed data
            file.writeText("$header\n")
            file.appendText(trimmedData.joinToString("\n") + "\n")

            Log.d("TrimCSVFile", "Trimmed CSV file to 50% of its original size")
        } catch (e: Exception) {
            Log.e("TrimCSVFile", "Error trimming CSV file: ${e.message}", e)
        }
    }


    fun saveUriListAsCSV(context: Context) {
        val csvFileName = "video_uri.csv"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName)

        try {
            // Build the CSV content from the URI list
            val csvBuilder = StringBuilder()
            csvBuilder.append("VideoUri\n")  // Add a header for the CSV file

            _uriList.value?.forEach { uri ->
                csvBuilder.append(uri.toString()).append("\n")  // Add each URI as a string
            }

            // Write the CSV content to the file
            FileOutputStream(filePath).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(csvBuilder.toString())
                }
            }

            Log.d("SaveUriListAsCSV", "CSV saved at: ${filePath.absolutePath}")

            // Update LiveData to reflect that the CSV has been saved
            state.value = state.value?.copy(csvUri = Uri.fromFile(filePath))
            state.postValue(state.value)
        } catch (e: Exception) {
            Log.e("SaveUriListAsCSV", "Error saving URI list as CSV: ${e.message}", e)
        }
    }

    fun saveToDatabase(context: Context) {
        val csvFileName = "video_uri.csv"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName)

        val roomEntity = RoomEntity(
            id = 0,
            videoUriFile = filePath.absolutePath,  // Save the absolute path instead of just the file name
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