import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.AccelerometerScreenVM
import kotlinx.coroutines.delay

@Composable
fun AccelerometerScreen(
    modifier: Modifier,
    sensorManager: SensorManager,
    changeAccData: (Float, Float, Float) -> Unit
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
        LaunchedEffect(Unit) {
            while (true) {
                delay(500) // 2-second delay
                changeAccData(accelerometerVM.x.value, accelerometerVM.y.value, accelerometerVM.z.value)
            }
        }

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