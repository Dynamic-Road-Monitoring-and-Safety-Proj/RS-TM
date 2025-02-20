import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.example.rstm.model.SensorData
import com.example.rstm.ui.screens.LocationScreen
import com.example.rstm.viewModels.ImplementVM
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImplementScreen(viewModel: ImplementVM, modifier: Modifier) {
    val context = LocalContext.current
    viewModel.getRepository().initializeUriList(context)

    var lensFacing by remember { mutableStateOf(LENS_FACING_BACK) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraxSelector = remember { CameraSelector.Builder().requireLensFacing(lensFacing).build() }

    var recording by remember { mutableStateOf<PendingRecording?>(null) }
    var onRecording by remember { mutableStateOf<Recording?>(null) }

    val recBuilder = Recorder.Builder()
    val qualitySelector = QualitySelector.fromOrderedList(listOf(Quality.FHD, Quality.HD, Quality.HIGHEST))
    val recorder = recBuilder.setQualitySelector(qualitySelector).build()
    val videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(recorder)

    val sensorData = remember { SensorData() }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val executor = Executors.newCachedThreadPool()

    // Launch Camera Provider Setup
    LaunchedEffect(lensFacing) {
        viewModel.fetchCameraProvider(context, lifecycleOwner, cameraxSelector, videoCapture)
    }

    LaunchedEffect(Unit) {
        val buffer = mutableListOf<SensorData>()

        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                buffer.add(sensorData.copy()) // Copy sensor data to prevent modification

                if (buffer.size >= 50) { // Adjust based on your needs
                    viewModel.getRepository().appendSensorDataToCSV(context, buffer)
                    buffer.clear() // Clear buffer after writing
                }

                delay(10) // Sample every 10 ms
            }
        }
    }


    // Video Recording Management
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                while (isActive) {
                    val result = viewModel.captureVideo(videoCapture, context)
                    val captureListener = result.second
                    recording = result.first
                    onRecording = recording?.start(executor, captureListener)

                    delay(6000) // Ensure it records fully

                    onRecording?.stop()
                    delay(40)
                }
            } catch (e: Exception) {
                Log.e("CameraPreviewScreen", "Error in video recording", e)
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            SensorSheetContent2C(
                data = sensorData,
                sensorManager = viewModel.getSensorManager(),
                fusedLocationClient = viewModel.getFusedLocation(),
                modifier = Modifier
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column {
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == LENS_FACING_BACK) LENS_FACING_FRONT else LENS_FACING_BACK
                    },
                    modifier = Modifier.offset(16.dp, 16.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Switch camera")
                }
                Spacer(modifier = Modifier.size(20.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Open gallery")
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            onRecording?.stop()
                            delay(100)

                            val result = viewModel.captureVideo(videoCapture, context)
                            val captureListener = result.second
                            recording = result.first

                            viewModel.getRepository().saveLastTwoVideosAndCSV(context)
                            viewModel.getRepository().saveToDatabase(context)

                            onRecording = recording?.start(executor, captureListener)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Record Last 30")
                }
            }
        }
    }
}

@Composable
fun SensorSheetContent2C(data: SensorData,sensorManager: SensorManager, fusedLocationClient : FusedLocationProviderClient, modifier: Modifier) {
    val sensorDataIMP = data

    fun changeGyroData(x: Float, y: Float, z: Float) {
        sensorDataIMP.gyroscopeData = Triple(x, y, z)
    }
    fun changeAccData(x: Float, y: Float, z: Float) {
        sensorDataIMP.accelerometerData = Triple(x, y, z)
    }
    fun changeMagData(x: Float, y: Float, z: Float) {
        sensorDataIMP.magneticData = Triple(x, y, z)
    }
    fun changeLightData(light: Float) {
        sensorDataIMP.lightData = light
    }
    fun changeLocationData(location: android.location.Location) {
        sensorDataIMP.locationData = location
    }

    GyroscopeScreen(modifier = modifier, sensorManager = sensorManager, function = ::changeGyroData)
    AccelerometerScreen(modifier = modifier, sensorManager, ::changeAccData)
    LightScreenComp(
        modifier = modifier,
        sensorManager = sensorManager,
        function = :: changeLightData
    )
    LocationScreen(fusedLocationClient = fusedLocationClient, function = ::changeLocationData)
}