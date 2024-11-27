//import android.bluetooth.BluetoothDevice
//import android.util.Log
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//
//@Composable
//fun BLEScreen(
//    receivedData: String,
//    startScan: (BluetoothDevice) -> Unit
//) {
//    var isScanning by remember { mutableStateOf(false) }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Text(
//            text = "BLE Data",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        Button(onClick = {
//            if (!isScanning) {
//                startScan {
//                    device ->
//                    // This block will be executed with a BluetoothDevice object
//                    Log.d("BLE", "Device found: ${device.name} (${device.address})")
//                }
//            }
//            isScanning = true
//        }) {
//            Text(text = if (isScanning) "Scanning..." else "Start Scanning")
//        }
//
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text(
//            text = "Received Data: $receivedData",
//            style = MaterialTheme.typography.bodyMedium,
//            modifier = Modifier.padding(top = 8.dp)
//        )
//    }
//}

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun BLEScreen() {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val devices = remember { mutableStateListOf<BluetoothDevice>() }
    val connectedDevice = remember { mutableStateOf<BluetoothDevice?>(null) }
    val receivedData = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        bluetoothAdapter?.bondedDevices?.let { pairedDevices ->
            devices.addAll(pairedDevices)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bluetooth ESP32") }) }
    ) { padding ->
        if (connectedDevice.value == null) {
            DeviceList(padding,devices) { device ->
                connectedDevice.value = device
            }
        } else {
            DataScreen(
                device = connectedDevice.value!!,
                onDisconnect = { connectedDevice.value = null },
                onReceiveData = { data -> receivedData.value = data }
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceList(paddingValues: PaddingValues, devices: List<BluetoothDevice>, onDeviceSelected: (BluetoothDevice) -> Unit) {
    LazyColumn {
        items(devices.size) { index ->
            val device = devices[index]
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceSelected(device) },
                headlineContent = { Text(device.name ?: "Unknown Device") },
                supportingContent = { Text(device.address) }
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DataScreen(
    device: BluetoothDevice,
    onDisconnect: () -> Unit,
    onReceiveData: (String) -> Unit
) {
    var isReceiving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.padding(16.dp)) {
        Text("Connected to: ${device.name}", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDisconnect) {
            Text("Disconnect")
        }
        Spacer(Modifier.height(16.dp))
        if (isReceiving) {
            Text("Receiving data...")
        } else {
            Button(onClick = {
                isReceiving = true
                coroutineScope.launch {
                    receiveData(device, onReceiveData)
                    isReceiving = false
                }
            }) {
                Text("Request Data")
            }
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun receiveData(device: BluetoothDevice, onReceiveData: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        val socket: BluetoothSocket? = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        try {
            socket?.connect()
            val inputStream: InputStream = socket?.inputStream ?: return@withContext
            val outputStream: OutputStream = socket.outputStream
            outputStream.write("REQUEST_DATA".toByteArray())

            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            val receivedString = String(buffer, 0, bytesRead)

            onReceiveData(receivedString)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            socket?.close()
        }
    }
}
