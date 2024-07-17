package com.example.rstm.ui.screens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@SuppressLint("SetTextI18n")
@Composable
fun CameraScreen(modifier: Modifier, lifecycleOwner: LifecycleOwner) {

    val recBuilder = Recorder.Builder()
    val qualitySelector = QualitySelector.fromOrderedList(
        listOf(Quality.FHD, Quality.HD, Quality.HIGHEST)
    )
    val recorder = recBuilder.setQualitySelector(qualitySelector).build()
    val videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(recorder)

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        lateinit var recording: Recording
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(factory = { ctx ->
                val relativeLayout = RelativeLayout(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }

                val previewView = PreviewView(ctx).apply {
                    id = View.generateViewId()
                    layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                }.controller =

                val startButton = android.widget.Button(ctx).apply {
                    id = View.generateViewId()
                    text = "Start Recording"
                    layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                        addRule(RelativeLayout.ALIGN_PARENT_START)
                        setMargins(16, 16, 16, 16)
                    }
                }

                val stopButton = android.widget.Button(ctx).apply {
                    id = View.generateViewId()
                    text = "Stop Recording"
                    layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                        addRule(RelativeLayout.ALIGN_PARENT_END)
                        setMargins(16, 16, 16, 16)
                    }
                }

                relativeLayout.addView(previewView)
                relativeLayout.addView(startButton)
                relativeLayout.addView(stopButton)

                startButton.setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        recording = startRecording(videoCapture, ctx, ContextCompat.getMainExecutor(ctx))
                        delay(20000)
                        recording.stop()
                    }
                }
                stopButton.setOnClickListener {
                    recording.stop()
                }

                // Initialize CameraX
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindPreview(cameraProvider, previewView, lifecycleOwner)
                }, ContextCompat.getMainExecutor(ctx))

                relativeLayout
            }, modifier = Modifier.fillMaxSize())
        }
    }
}

fun bindPreview(cameraProvider: ProcessCameraProvider, previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
    } catch (e: Exception) {
        Log.d("CameraScreen", "Error binding camera provider", e)
    }
}

@SuppressLint("MissingPermission")
suspend fun startRecording(
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
