import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.AccelerometerScreenVM

@Composable
fun AccelerometerScreen(
    modifier: Modifier,
    sensorManager:SensorManager
) {
    val accelerometerVM: AccelerometerScreenVM = viewModel()
    val accelerometer: Sensor? = accelerometerVM.accelerometer

    DisposableEffect(sensorManager) {
        accelerometerVM.startAccelerometer(sensorManager)
        onDispose {
            accelerometerVM.stopAccelerometer(sensorManager)
        }
    }

    Column(modifier) {
        Text(text = "Accelerometer Screen", modifier = modifier.fillMaxWidth())
        if (accelerometer != null) {
            Text(text = "Accelerometer is available")
        } else {
            Text(text = "Accelerometer is not available")
        }
        Text(text = "x: ${accelerometerVM.x.value}")
        Text(text = "y: ${accelerometerVM.y.value}")
        Text(text = "z: ${accelerometerVM.z.value}")
    }
}