import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class BluetoothViewModel : ViewModel() {

    var connectedDevice: BluetoothDevice? = null
        private set

    var bluetoothSocket: BluetoothSocket? = null
        private set

    val receivedDataList = mutableListOf<String>()
    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(
        device: BluetoothDevice,
        onReceiveData: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        connectedDevice = device
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val socket = device.createRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                )
                socket.connect()
                bluetoothSocket = socket

                receiveData(socket) { data ->
                    receivedDataList.add(data)
                    onReceiveData(data)
                }

            } catch (e: IOException) {
                onError(e)
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            bluetoothSocket?.close()
            bluetoothSocket = null
            connectedDevice = null
        }
    }

    private fun receiveData(socket: BluetoothSocket, onReceiveData: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream = socket.inputStream
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
    fun clearData() {
        receivedDataList.clear()
    }

    fun saveDataToCSV(file: File, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                FileOutputStream(file, false).use { fos ->
                    receivedDataList.forEach { line ->
                        fos.write((line + "\n").toByteArray())
                    }
                }
                onSuccess()
            } catch (e: IOException) {
                e.printStackTrace()
                onError(e)
            }
        }
    }
}
