package com.example.rstm.ui.screens

import android.content.Context
import android.hardware.SensorManager
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.CameraScreenVM

@Composable
fun Activated(modifier: Modifier, sensorManager: SensorManager, appContext: Context){

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
}
