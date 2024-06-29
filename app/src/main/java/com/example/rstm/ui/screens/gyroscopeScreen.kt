import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.GyroViewModel
import com.example.rstm.viewModels.LightViewModel

@Composable
fun GyroscopeScreen(modifier: Modifier, sensorManager:SensorManager) {
    val viewModel: GyroViewModel = viewModel()

    DisposableEffect(sensorManager) {
        viewModel.startGyroSensor(sensorManager)
        onDispose {
            viewModel.stopGyroSensor(sensorManager)
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
