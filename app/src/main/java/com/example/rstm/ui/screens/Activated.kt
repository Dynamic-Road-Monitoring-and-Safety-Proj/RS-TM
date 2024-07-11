package com.example.rstm.ui.screens

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.CameraScreenVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Activated(modifier: Modifier, sensorManager: SensorManager,context: Context, appContext: Context){

    val viewModel: CameraScreenVM = viewModel()
    val scope = rememberCoroutineScope()
    val controller = remember {
        LifecycleCameraController(appContext).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }
    controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    IconButton(onClick = {
        CoroutineScope.launch(Dispatchers.Default)
        {
            viewModel.recordVideo(controller, context, appContext)
            delay(20000)
            viewModel.recordVideo(controller, context, appContext)
        }
    }) {
        Icon(imageVector = Icons.Filled.Warning, contentDescription = "Trigger 20 sec recording" )
    }
}
