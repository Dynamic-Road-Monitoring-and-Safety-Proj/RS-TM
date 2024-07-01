package com.example.rstm.ui.screens

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
@Composable
fun CameraScreen(context: Context, lifecycleOwner: LifecycleOwner) {

    AndroidView(factory = { ctx ->
        val cameraView = FrameLayout(ctx)
        cameraView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val previewView = PreviewView(ctx)
        previewView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val button = android.widget.Button(ctx)
        button.text = "Take Picture"
        button.setOnClickListener {
            // Take Picture
        }

        cameraView.addView(previewView)
        cameraView.addView(button)

        // Initialize CameraX
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider, previewView, lifecycleOwner)
        }, ContextCompat.getMainExecutor(ctx))

        cameraView
    })
    
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
        // Handle exceptions
        Log.d("CameraScreen", "Error binding camera provider", e)
    }
}
