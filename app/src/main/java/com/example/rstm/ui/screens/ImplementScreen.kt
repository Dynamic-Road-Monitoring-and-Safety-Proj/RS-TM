import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.rstm.viewModels.ImplementVM
import com.example.rstm.viewModels.RecordingState

@Composable
fun ImplementScreen(
    viewModel: ImplementVM,  // ViewModel reference
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lensFacing by viewModel.lensFacing.observeAsState(CameraSelector.LENS_FACING_BACK)
    val recordingState by viewModel.recordingState.observeAsState(RecordingState.Stopped)
    val uriList by viewModel.uriList.observeAsState(emptyList())

    // Start camera when the lensFacing changes
    LaunchedEffect(lensFacing) {
        viewModel.startCamera(context, lifecycleOwner)
    }

    // Clean up camera when the screen goes out of scope
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRecording()
        }
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
        LazyColumn {
            items(uriList) { uri ->
                Text(text = uri.toString())
            }
        }

        // Recording controls: Toggle between record and stop icons
        IconButton(onClick = { viewModel.toggleRecording(context) }) {
            Icon(
                imageVector = when (recordingState) {
                    is RecordingState.Started -> Icons.Default.ArrowDropDown
                    is RecordingState.Stopped -> Icons.Default.PlayArrow
                    else -> {
                        Icons.Default.Close
                    }
                },
                contentDescription = when (recordingState) {
                    is RecordingState.Started -> "Stop Recording"
                    is RecordingState.Stopped -> "Start Recording"
                    else -> {}
                }.toString()
            )
        }
    }
}