import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.GyroViewModel
import kotlinx.coroutines.delay

@Composable
fun GyroscopeScreen(
    modifier: Modifier,
    sensorManager: SensorManager,
    function: (Float, Float, Float) -> Unit
) {
    val viewModel: GyroViewModel = viewModel()

    DisposableEffect(sensorManager) {
        viewModel.startGyroSensor(sensorManager)
        onDispose {
            viewModel.stopGyroSensor(sensorManager)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            function(viewModel.x1.floatValue, viewModel.y1.floatValue, viewModel.z1.floatValue)
            delay(100)
        }
    }

    Column(modifier) {
        Text(text = "Gyroscope Screen", modifier = modifier.fillMaxWidth())
        Text(text = if (viewModel.gyroscope != null) "GyroS is available" else "GyroS is not available")
        Text(text = "x: ${viewModel.x1.floatValue}")
        Text(text = "y: ${viewModel.y1.floatValue}")
        Text(text = "z: ${viewModel.z1.floatValue}")
    }
}
