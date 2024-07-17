package com.example.rstm.ui.screens

import AccelerometerScreen
import GyroscopeScreen
import LightScreenComp
import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.CameraScreenVM
import com.example.rstm.viewModels.MagneticFieldScreenVM
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)


    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    fun onCreate() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun onStart() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun onResume() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onPause() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun onStop() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Activated(
    modifier: Modifier,
    sensorManager: SensorManager,
    context: Context,
    appContext: Context,
    fusedLocationClient: FusedLocationProviderClient
) {
    val viewModel: CameraScreenVM = viewModel()
    val scope = rememberCoroutineScope()
    val controller = remember {
        LifecycleCameraController(appContext).apply {
            setEnabledUseCases(CameraController.VIDEO_CAPTURE)
        }
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val lifecycleOwner = remember { CustomLifecycleOwner() }

    // Manage the lifecycle state of the custom LifecycleOwner
    DisposableEffect(Unit) {
        lifecycleOwner.onCreate()
        lifecycleOwner.onStart()
        lifecycleOwner.onResume()
        onDispose {
            lifecycleOwner.onPause()
            lifecycleOwner.onStop()
            lifecycleOwner.onDestroy()
        }
    }

    controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    controller.bindToLifecycle(lifecycleOwner)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 20.dp,
        sheetContent = {
            SensorSheetContent(sensorManager, fusedLocationClient, Modifier.fillMaxWidth())
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // You can remove the CameraPreview2 composable and instead directly add controls
            Row {
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            scope.launch {
                                viewModel.recordVideo(controller, context, appContext)
                                Toast.makeText(context, "started", LENGTH_SHORT).show()
                                delay(20000)
                                Toast.makeText(context, "ended", LENGTH_SHORT).show()
                                viewModel.recordVideo(controller, context, appContext)
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
fun SensorSheetContent(
    sensorManager: SensorManager,
    fusedLocationClient: FusedLocationProviderClient,
    modifier: Modifier
) {
    GyroscopeScreen(modifier = modifier, sensorManager = sensorManager)
    AccelerometerScreen(modifier = modifier, sensorManager)
    LightScreenComp(modifier = modifier, sensorManager = sensorManager)
    LocationScreen(fusedLocationClient = fusedLocationClient)
    magFieldScreen(modifier = modifier, sensorManager = sensorManager)
}
