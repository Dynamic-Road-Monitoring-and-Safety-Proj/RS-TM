import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Environment
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun BLEScreen() {
    val viewModel: BluetoothViewModel = viewModel()
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
            DeviceList(padding, devices) { device ->
                connectedDevice.value = device
            }
        } else {
            DataScreen(
                viewModel = viewModel,
                device = connectedDevice.value!!,
                onDisconnect = { connectedDevice.value = null },
                onReceiveData = { data -> receivedData.value = data }
            )
        }
    }
}
@Composable
fun SaveButton(receivedDataList: List<String>) {
    Button(onClick = {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "bluetooth_data.csv"
        )
        try {
            FileOutputStream(file, false).use { fos ->
                receivedDataList.forEach { line ->
                    fos.write((line + "\n").toByteArray())
                }
            }
            Log.d("Save", "CSV saved!")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Save", "Failed: ${e.message}")
        }
    }) {
        Text("Save CSV")
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
    onReceiveData: (String) -> Unit,
    viewModel: BluetoothViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val receivedDataList = remember { mutableStateListOf<String>() }
    val bluetoothSocket = remember {
        mutableStateOf<BluetoothSocket?>(null)
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                socket.connect()
                bluetoothSocket.value = socket

                // Start receiving immediately after connecting
                coroutineScope.launch {
                    receiveData(socket) { data ->
                        receivedDataList.add(data)
                        onReceiveData(data)
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    Column(Modifier.padding(top = 100.dp, start = 30.dp)) {
        Text("Connected to: ${device.name}", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Row{
            Button(onClick = {
                bluetoothSocket.value?.close()
                onDisconnect()
            }) {
                Text("Disconnect")
            }
            SaveButton(receivedDataList)
        }

        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(receivedDataList.size) { index ->
                Text(text = receivedDataList[index])
            }
        }
    }
}


suspend fun receiveData(socket: BluetoothSocket?, onReceiveData: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = socket?.inputStream ?: return@withContext
            val buffer = ByteArray(1024)

            while (true) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead > 0) {
                    val receivedString = String(buffer, 0, bytesRead)
                    onReceiveData(receivedString)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}