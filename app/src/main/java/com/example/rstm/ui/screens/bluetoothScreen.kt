import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun BLEScreen(
    receivedData: String,
    startScan: (BluetoothDevice) -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "BLE Data",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = {
            if (!isScanning) {
                startScan {
                    device ->
                    // This block will be executed with a BluetoothDevice object
                    Log.d("BLE", "Device found: ${device.name} (${device.address})")
                }
            }
            isScanning = true
        }) {
            Text(text = if (isScanning) "Scanning..." else "Start Scanning")
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Received Data: $receivedData",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
