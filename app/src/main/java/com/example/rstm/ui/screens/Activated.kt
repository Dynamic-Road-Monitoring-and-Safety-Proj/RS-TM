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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.CameraScreenVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Activated(
    modifier: Modifier,
    sensorManager: SensorManager,
    context: Context,
    appContext: Context
){
//    val scope = rememberCoroutineScope()
//    val viewModel: CameraScreenVM = viewModel()
//    val controller = remember {
//        LifecycleCameraController(appContext).apply {
//            setEnabledUseCases(
//                CameraController.IMAGE_CAPTURE or
//                        CameraController.VIDEO_CAPTURE
//            )
//        }
//    }
//    controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//    Scaffold { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            CameraPreview(
//                controller = controller,
//                modifier = Modifier.fillMaxSize()
//            )
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceAround
//            ) {
//                IconButton(
//                    onClick = {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            scope.launch() {
//                                viewModel.recordVideo(controller, context, appContext)
//                                Toast.makeText(context, "started", LENGTH_SHORT).show()
//                                delay(20000)
//                                Toast.makeText(context, "ended", LENGTH_SHORT).show()
//                                viewModel.recordVideo(controller, context, appContext)
//                            }
//                        }
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.PlayArrow,
//                        contentDescription = "Record video"
//                    )
//                }
//            }
//        }
//    }

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
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        scope.launch() {
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

