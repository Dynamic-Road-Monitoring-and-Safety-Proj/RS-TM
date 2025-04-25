import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Environment
import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun SaveButton(receivedDataList: List<String>, modifier: Modifier = Modifier) {
    FloatingActionButton (modifier = modifier, onClick = {
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val bluetoothSocket = remember { mutableStateOf<BluetoothSocket?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                socket.connect()
                bluetoothSocket.value = socket

                coroutineScope.launch {
                    receiveData(socket) { data ->
                        receivedDataList.add(0, data) // add latest at top
                        onReceiveData(data)
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1E1E2F), Color(0xFF121212))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            TopAppBar(
                title = { Text("Connected: ${device.name}", color = Color.White) },
                actions = {
                    IconButton(onClick = {
                        bluetoothSocket.value?.close()
                        onDisconnect()
                    }) {
                        Icon(Icons.Rounded.Build, contentDescription = "Disconnect", tint = Color.Red)
                    }
                },
            )

            Spacer(Modifier.height(16.dp))
            AnimatedSensorCard("Time, Gyro X, Gyro Y, Gyro Z, Accel X, Accel Y, Accel Z")
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(receivedDataList.size) { index ->
                    val data = receivedDataList[index]
                    AnimatedSensorCard(data = data)
                }
            }
        }

        SaveButton(
            receivedDataList,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}
@Composable
fun AnimatedSensorCard(data: String) {
    val bgColor = remember { Color(0xFF2C2C3A) }
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(700, easing = LinearOutSlowInEasing),
        label = "fade-in"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animatedAlpha),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            text = data,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
            color = Color.White
        )
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