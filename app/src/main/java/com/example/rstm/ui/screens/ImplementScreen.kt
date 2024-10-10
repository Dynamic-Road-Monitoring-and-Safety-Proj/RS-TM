import android.hardware.SensorManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.util.Consumer
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.rstm.ui.screens.LocationScreen
import com.example.rstm.viewModels.ImplementVM
import com.example.rstm.viewModels.RecordingState
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImplementScreen(
    viewModel: ImplementVM,
    Modifier: Modifier
) {
    val context = LocalContext.current
    viewModel.getRepository().initializeUriList(context)
    val uriList by viewModel.getRepository().uriList.observeAsState(emptyList())

    var lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    var recording by remember { mutableStateOf<PendingRecording?>(null) }
    var onRecording by remember { mutableStateOf<Recording?>(null) }
    val recBuilder = Recorder.Builder()
    val qualitySelector = QualitySelector.fromOrderedList(
        listOf(Quality.FHD, Quality.HD, Quality.HIGHEST)
    )

    val recorder = recBuilder.setQualitySelector(qualitySelector).build()
    val videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(recorder)

    LaunchedEffect(lensFacing) {
        viewModel.fetchCameraProvider(
            context = context,
            lifecycleOwner,
            cameraxSelector,
            videoCapture
        )
    }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val executor = Executors.newCachedThreadPool()
    var captureListener : Consumer<VideoRecordEvent>

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                while (true) {
                    async {
                        val result = viewModel.captureVideo(videoCapture, context)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "starting", Toast.LENGTH_SHORT).show()
                        }

                        captureListener = result.second
                        recording = result.first
                        onRecording = recording?.start(
                            executor,
                            captureListener
                        )

                        delay(10000)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "stopping", Toast.LENGTH_SHORT).show()
                        }

                        onRecording?.stop()

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "stopped", Toast.LENGTH_SHORT).show()
                        }

                        delay(1000)
                    }.await()
                }
            } catch (e: Exception) {
                Log.e("CameraPreviewScreen", "Error starting video recording", e)
            }
        }
    }


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            SensorSheetContent2C(sensorManager = viewModel.getSensorManager(), fusedLocationClient = viewModel.getFusedLocation() , modifier = Modifier)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column{
                IconButton(
                    onClick = {
                        lensFacing =
                            if (lensFacing == LENS_FACING_BACK) {
                                LENS_FACING_FRONT
                            } else LENS_FACING_BACK
                    },
                    modifier = Modifier.offset(16.dp, 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Switch camera"
                    )
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
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Open gallery"
                    )
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            onRecording?.stop() // Stop the current recording

                            delay(1000) // Small delay to ensure stop completes

                            // Add the last recorded video to the list (handled in the captureListener already)
                            val result = viewModel.captureVideo(videoCapture, context)
                            captureListener = result.second
                            recording = result.first
                            viewModel.getRepository().saveToDatabase(context)
                            // Start a new buffered recording
                            onRecording = recording?.start(
                                executor,
                                captureListener
                            )
                        }
                        Log.e("sdaf", "_____________________________${uriList.size}    : $uriList")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Record Last 30"
                    )
                }
            }
        }
    }
}

@Composable
fun SensorSheetContent2C(sensorManager: SensorManager, fusedLocationClient : FusedLocationProviderClient, modifier: Modifier) {
    GyroscopeScreen(modifier = modifier, sensorManager = sensorManager)
    AccelerometerScreen(modifier = modifier, sensorManager)
    LightScreenComp(modifier = modifier, sensorManager = sensorManager )
    LocationScreen(fusedLocationClient = fusedLocationClient)
}