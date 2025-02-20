import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.rstm.MainActivity
import com.example.rstm.model.SensorData
import com.example.rstm.model.State
import com.example.rstm.roomImplementation.RoomEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.sql.Timestamp

class ImplementRepository() {

    private val state = MutableLiveData(State())

    private val _uriList = MutableLiveData<List<Uri>>(emptyList())

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

        for (i in 0..1) {
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

    fun appendSensorDataToCSV(context: Context, data: SensorData) {
        val csvFileName = "sensor_data.csv"
        val maxFileSize = 100 * 1024 * 1024 // 100 MB in bytes
        val maxReadings = 40000

        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, csvFileName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, "text/csv")
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }

            val existingUri = findFileUri(context, csvFileName)
            val fileUri = existingUri ?: resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
            ?: throw Exception("Failed to create file URI in MediaStore")

            val isFileEmpty = resolver.openInputStream(fileUri)?.bufferedReader()?.use { it.readLine() } == null
            val header = "Timestamp,Accelerometer_X,Accelerometer_Y,Accelerometer_Z," +
                    "Gyroscope_X,Gyroscope_Y,Gyroscope_Z,Light,Latitude,Longitude,Altitude,Speed\n"
            val sensorData = data.copy(timestamp = Timestamp(System.currentTimeMillis()))
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
            resolver.openOutputStream(fileUri, "wa")?.use { outputStream ->
                if (isFileEmpty) {
                    outputStream.write(header.toByteArray(Charsets.UTF_8)) // Write header first
                }
                outputStream.write(csvLine.toByteArray(Charsets.UTF_8)) // Append sensor data
            }

            Log.d("AppendSensorDataToCSV", "CSV saved at URI: $fileUri")


            if (getFileSize(context, fileUri) > maxFileSize || countReadings(context, fileUri) > maxReadings) {
                trimCSVFile(context, fileUri)
            }
        } catch (e: Exception) {
            Log.e("AppendSensorDataToCSV", "Error appending data: ${e.message}", e)
        }
    }

    private fun findFileUri(context: Context, fileName: String): Uri? {
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ? AND ${MediaStore.Files.FileColumns.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(fileName, Environment.DIRECTORY_DOCUMENTS + "/")

        context.contentResolver.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                return Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
            }
        }
        return null
    }


    private fun getFileSize(context: Context, uri: Uri): Long {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            return pfd.statSize
        }
        return 0L
    }

    private fun countReadings(context: Context, uri: Uri): Int {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            return inputStream.bufferedReader().lineSequence().count() - 1
        }
        return 0
    }

    // Function to trim the first 50% of data from the CSV file
    fun trimCSVFile(context: Context, fileUri: Uri) {
        try {
            val resolver = context.contentResolver

            // Read the file content
            val lines = resolver.openInputStream(fileUri)?.bufferedReader()?.readLines() ?: emptyList()
            if (lines.isEmpty()) return

            val header = lines.first()
            val data = lines.drop(1) // Exclude the header
            val trimmedData = data.drop(data.size / 2) // Drop the first 50% of rows

            // Rewrite the file with the header and trimmed data
            resolver.openOutputStream(fileUri, "wt")?.bufferedWriter()?.use { writer ->
                writer.write(header)
                writer.newLine()
                trimmedData.forEach { writer.write(it); writer.newLine() }
            }

            Log.d("TrimCSVFile", "Trimmed CSV file to 50% of its original size")
        } catch (e: Exception) {
            Log.e("TrimCSVFile", "Error trimming CSV file: ${e.message}", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun saveLastTwoVideosAndCSV(context: Context) {
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val dcimDir = "DCIM/triggered"
            val timestamp = System.currentTimeMillis()
            val csvFileName = "video_uri_$timestamp.csv"

            try {
                val uriList = _uriList.value ?: emptyList()
                if (uriList.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No videos available to save", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("SaveToDCIM", "No videos available to save")
                    return@withContext
                }

                val lastTwoUris = uriList.takeLast(2)

                val csvBuilder = StringBuilder()
                csvBuilder.append("VideoUri\n")

                for ((index, uri) in lastTwoUris.withIndex()) {
                    val fileName = "Recording_${timestamp}_$index.mp4"
                    csvBuilder.append(uri.toString()).append("\n")

                    val contentValues = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        put(MediaStore.Video.Media.RELATIVE_PATH, dcimDir)
                    }

                    val newUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (newUri == null) {
                        Log.e("SaveToDCIM", "Failed to create new file for $fileName")
                        continue
                    }

                    resolver.openInputStream(uri)?.use { inputStream ->
                        resolver.openOutputStream(newUri)?.use { outputStream ->
                            inputStream.copyTo(outputStream)
                            Log.d("SaveToDCIM", "Successfully copied: $fileName")
                        }
                    } ?: Log.e("SaveToDCIM", "Error opening input stream for $uri")
                }

                // Fix: Use Files.getContentUri("external") instead of Downloads
                val csvContentValues = ContentValues().apply {
                    put(MediaStore.Files.FileColumns.DISPLAY_NAME, csvFileName)
                    put(MediaStore.Files.FileColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.Files.FileColumns.RELATIVE_PATH, dcimDir)

                }

                val csvUri = resolver.insert(MediaStore.Files.getContentUri("external"), csvContentValues)
                if (csvUri != null) {
                    resolver.openOutputStream(csvUri)?.use { outputStream ->
                        outputStream.write(csvBuilder.toString().toByteArray())
                    }
                    Log.d("SaveCSV", "CSV saved as: $csvFileName in DCIM/triggered")
                } else {
                    Log.e("SaveCSV", "Failed to create CSV file")
                }


                // Show success message on main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Videos & CSV saved successfully!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("SaveToDCIM", "Error saving files: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error saving files: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
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