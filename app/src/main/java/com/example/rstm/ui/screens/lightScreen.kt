
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.LightViewModel

@Composable
fun LightScreenComp(modifier: Modifier, sensorManager:SensorManager) {
    val viewModel: LightViewModel = viewModel()
    val lightValue = viewModel.lightValue

    // Ensure the sensor is started when the composable is first created
    DisposableEffect(sensorManager) {
        viewModel.startLightSensor(sensorManager)
        onDispose {
            viewModel.stopLightSensor(sensorManager)
        }
    }
    Column(modifier) {
        Text(text = "Light brightness Screen", modifier = modifier.fillMaxWidth())
        Text(text = if (viewModel.lightSensor != null) "LightS is available" else "LightS is not available")
        Text(text = "Luminosity: ${lightValue.value}")
    }
}
