package com.example.rstm.ui.screens

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.CameraScreenVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Activated(
    modifier: Modifier,
    sensorManager: SensorManager,
    context: Context,
    appContext: Context
){
//    val viewModel: CameraScreenVM = viewModel()
//    val scope = rememberCoroutineScope()
//    val controller = remember {
//        LifecycleCameraController(appContext).apply {
//            setEnabledUseCases(
//                CameraController.IMAGE_CAPTURE or
//                        CameraController.VIDEO_CAPTURE
//            )
//        }
//    }
//    val lifecycleOwner = LocalLifecycleOwner.current
//    controller.bindToLifecycle(lifecycleOwner)
//
//    controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//    IconButton(onClick = {
//        scope.launch(Dispatchers.Main) {
//            viewModel.recordVideo(controller, context, appContext)
//            Toast.makeText(context, "started", LENGTH_SHORT).show()
//            delay(20000)
//            Toast.makeText(context, "ended", LENGTH_SHORT).show()
//            viewModel.recordVideo(controller, context, appContext)
//        }
//    }) {
//        Box(
//            modifier = modifier
//                .padding(30.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Filled.Warning,
//                contentDescription = "Trigger 20 sec recording"
//            )
//        }
//    }
}
