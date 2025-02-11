package com.example.rstm.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.rstm.yolo.BoundingBox
import com.example.rstm.yolo.Detector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun YoloDetectionScreen(
    modelPath: String,
    labelsPath: String,
    onPermissionDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var detectionResults by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }
    var inferenceTime by remember { mutableStateOf(0L) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val detector = remember {
        Detector(context, modelPath, labelsPath, object : Detector.DetectorListener {
            override fun onEmptyDetect() {
                Log.d("YoloDetection", "No detections found")
                detectionResults = emptyList()
            }

            override fun onDetect(boundingBoxes: List<BoundingBox>, time: Long) {
                Log.d("YoloDetection", "Detected ${boundingBoxes.size} objects in ${time}ms")
                Log.d("YoloDetection", "First box: ${boundingBoxes.firstOrNull()}")
                detectionResults = boundingBoxes
                inferenceTime = time
            }
        })
    }

    DisposableEffect(Unit) {
        detector.setup()
        onDispose {
            detector.clear()
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            detector = detector,
            cameraExecutor = cameraExecutor,
            onPermissionDenied = onPermissionDenied
        )

        DetectionOverlay(
            boundingBoxes = detectionResults,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = "${inferenceTime}ms",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun CameraPreview(
    detector: Detector,
    cameraExecutor: ExecutorService,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .apply {
                    setAnalyzer(cameraExecutor) { imageProxy ->
                        val bitmapBuffer = Bitmap.createBitmap(
                            imageProxy.width,
                            imageProxy.height,
                            Bitmap.Config.ARGB_8888
                        )
                        imageProxy.use {
                            bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
                        }
                        imageProxy.close()

                        val matrix = Matrix().apply {
                            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                        }

                        val rotatedBitmap = Bitmap.createBitmap(
                            bitmapBuffer,
                            0,
                            0,
                            bitmapBuffer.width,
                            bitmapBuffer.height,
                            matrix,
                            true
                        )

                        detector.detect(rotatedBitmap)
                    }
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            camera = null
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun DetectionOverlay(
    boundingBoxes: List<BoundingBox>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        Log.d("YoloDetection", "Drawing ${boundingBoxes.size} boxes at size ${size.width}x${size.height}")
        boundingBoxes.forEach { box ->
            val left = box.x1 * size.width
            val top = box.y1 * size.height
            Log.d("YoloDetection", "Drawing box at ($left, $top)")
            drawBoundingBox(box)
            drawLabel(box)
        }
    }
}

private fun DrawScope.drawBoundingBox(box: BoundingBox) {
    val left = box.x1 * size.width
    val top = box.y1 * size.height
    val right = box.x2 * size.width
    val bottom = box.y2 * size.height

    drawRect(
        color = Color(0xFF2196F3),
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
        style = Stroke(width = 8f)
    )
}

private fun DrawScope.drawLabel(box: BoundingBox) {
    val left = box.x1 * size.width
    val top = box.y1 * size.height

    drawRect(
        color = Color.Black,
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(100f, 30f)
    )

    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
        }
        canvas.nativeCanvas.drawText(
            box.clsName,
            left,
            top + 25f,
            paint
        )
    }
}

// Permission handling composable
@Composable
fun RequestCameraPermission(
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        onPermissionGranted()
    } else {
        onPermissionDenied()
    }
}


// Usage example
@Composable
fun YoloDetectionRoute(
    modelPath: String,
    labelsPath: String,
    onPermissionDenied: () -> Unit
) {
    RequestCameraPermission(
        onPermissionGranted = {
            YoloDetectionScreen(
                modelPath = modelPath,
                labelsPath = labelsPath,
                onPermissionDenied = onPermissionDenied
            )
        },
        onPermissionDenied = onPermissionDenied
    )
}