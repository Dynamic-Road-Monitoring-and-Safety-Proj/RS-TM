package com.example.rstm.ui.screens

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.rstm.viewModels.ImplementVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun ImplementScreen(
    viewModel: ImplementVM,  // ViewModel reference
    modifier: Modifier
 ) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe ViewModel state
    val lensFacing by viewModel.lensFacing.observeAsState(CameraSelector.LENS_FACING_BACK)
    val recordingState by viewModel.recordingState.observeAsState()
    val uriList by viewModel.uriList.observeAsState(emptyList())

    LaunchedEffect(lensFacing) {
        viewModel.startCamera(context, lifecycleOwner)
    }

    // UI Elements
    Box(modifier = modifier.fillMaxSize()) {
        // Switch camera button
        IconButton(onClick = { viewModel.toggleLensFacing() }) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Switch camera"
            )
        }

        // Recording controls, use ViewModel to handle recording start/stop
        IconButton(onClick = { viewModel.toggleRecording(context) }) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Record"
            )
        }
    }
}