package com.example.rstm.ui.screens

import AccelerometerScreen
import GyroscopeScreen
import LightScreenComp
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.hardware.SensorManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.core.util.Consumer
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPreviewScreen(
    sensorManager: SensorManager,
    fusedLocationClient: FusedLocationProviderClient,
    Modifier: Modifier
) {
    var URIlist : MutableList<Uri> = mutableListOf()

    var lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
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
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, videoCapture)
    }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val executor = Executors.newSingleThreadExecutor()
    var captureListener : Consumer<VideoRecordEvent>
    LaunchedEffect(Unit) {
        executor.execute {
            try {
                while(true) {
                    if(URIlist.size >= 6) {
                        val uri = URIlist[0]
                        val contentResolver = context.contentResolver
                        val deleted = contentResolver.delete(URIlist[0], null, null)
                        if (deleted > 0) {
                            Log.d("DeleteVideo", "Video deleted successfully: $uri")
                        } else {
                            Log.e("DeleteVideo", "Failed to delete video: $uri")
                        }
                    }
                    val result = captureVideo(videoCapture, context, URIlist)
                    captureListener = result.second
                    recording = result.first // Assign to the existing `recording`
                    onRecording = recording?.start(
                        executor,
                        captureListener
                    )
                    scope.launch {
                        delay(1000)
                        onRecording?.stop()
                    }
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
            SensorSheetContent2(sensorManager = sensorManager, fusedLocationClient = fusedLocationClient, modifier = Modifier)
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
                        // Stop recording on the main thread
                        scope.launch {
                            onRecording?.stop()
                            recording = null
                            Toast.makeText(context, "done", LENGTH_SHORT).show()
                            // Ensure the executor shuts down after tasks are complete
                            executor.shutdown()
                            try {
                                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                                    executor.shutdownNow()
                                }
                            } catch (e: InterruptedException) {
                                executor.shutdownNow()
                                Thread.currentThread().interrupt()
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Record video"
                    )
                }
            }
        }
    }
}
@Composable
fun SensorSheetContent2(sensorManager: SensorManager, fusedLocationClient : FusedLocationProviderClient, modifier: Modifier) {
    GyroscopeScreen(modifier = modifier, sensorManager = sensorManager)
    AccelerometerScreen(modifier = modifier, sensorManager)
    LightScreenComp(modifier = modifier, sensorManager = sensorManager )
    LocationScreen(fusedLocationClient = fusedLocationClient)
}

@SuppressLint("MissingPermission")
private fun captureVideo(
    videoCapture: VideoCapture<Recorder>,
    context: Context,
    URIlist: MutableList<Uri>
): Pair<PendingRecording, Consumer<VideoRecordEvent> >{
    if(URIlist.size >= 6){
        for (i in 0..4){
            URIlist[i] = URIlist[i+1]
        }
        URIlist.removeAt(5)
    }
    var index = URIlist.size%6

    //names will be like 0.mp4 to 5.mp4
    val name = "$index.mp4"
    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, name)
    }
    val mediaStoreOutput = MediaStoreOutputOptions.Builder(context.contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
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
                    val videoUri = event.outputResults.outputUri
                    if (URIlist.size < 6) {
                        URIlist.add(videoUri)
                    } else {
                        URIlist[5] = videoUri
                    }
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

//        .start(executor, captureListener)
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }