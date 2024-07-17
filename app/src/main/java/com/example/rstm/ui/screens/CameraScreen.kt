package com.example.rstm.ui.screens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("SetTextI18n")
@Composable
fun CameraScreen(modifier: Modifier, lifecycleOwner: LifecycleOwner) {

    val recBuilder = Recorder.Builder()
    val qualitySelector = QualitySelector.fromOrderedList(
        listOf(Quality.FHD, Quality.HD, Quality.HIGHEST)
    )
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val recorder = recBuilder.setQualitySelector(qualitySelector).build()
    val videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(recorder)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    lateinit var recording: Recording

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, videoCapture)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Button(onClick = {
            recording = startRecording(videoCapture, context, ContextCompat.getMainExecutor(context))
            CoroutineScope(Dispatchers.Main).launch {
                delay(20000)
                recording.stop()
                Toast.makeText(context,"done", LENGTH_SHORT).show()
            }
        }
        ){
            Text(text = "Capture Video 20 sec")
        }

    }
}

@SuppressLint("MissingPermission")
fun startRecording(
    videoCapture: VideoCapture<Recorder>,
    context: Context,
    executor: Executor
): Recording {
    val name = "CameraX-recording-" +
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".mp4"
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
                } else {
                    Log.e("CameraScreen", "Video recording failed: ${event.cause}")
                }
            }
            else -> {
                // Handle other events if needed
            }
        }
    }

    return videoCapture.output
        .prepareRecording(context, mediaStoreOutput)
        .withAudioEnabled()
        .start(executor, captureListener)
}

private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }