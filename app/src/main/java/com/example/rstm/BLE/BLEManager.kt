package com.example.rstm

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import java.util.*

class BLEManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null
    private var onDataReceived: ((String) -> Unit)? = null

    private val YOUR_SERVICE_UUID = UUID.fromString("0000181A-0000-1000-8000-00805f9b34fb") // Replace with your service UUID
    private val YOUR_CHARACTERISTIC_UUID = UUID.fromString("00002A57-0000-1000-8000-00805f9b34fb") // Replace with your characteristic UUID
    private val CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Start scanning for BLE devices
    @SuppressLint("MissingPermission")
    fun startScanning(onDeviceFound: ((BluetoothDevice) -> Unit)) {
        scanner.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val device = result?.device
                if (device != null) {
                    Log.d("BLE", "Device found: ${device.name} (${device.address})")
                    onDeviceFound(device)
                }
            }
        })
    }

    // Connect to the device and set up notifications
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice, onDataReceived: (String) -> Unit) {
        this.onDataReceived = onDataReceived
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    // GATT callback to handle connection, services, and incoming data
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Connected to GATT server")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLE", "Disconnected from GATT server")
                bluetoothGatt = null
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(YOUR_SERVICE_UUID)
                val characteristic = service?.getCharacteristic(YOUR_CHARACTERISTIC_UUID)

                if (characteristic != null) {
                    // Enable notifications for the characteristic
                    gatt.setCharacteristicNotification(characteristic, true)

                    // Write descriptor to enable notifications
                    val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)

                    Log.d("BLE", "Notifications enabled for characteristic")
                } else {
                    Log.e("BLE", "Characteristic not found")
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            // Receive data when the characteristic changes
            val data = characteristic.value
            val receivedData = data?.toString(Charsets.UTF_8) ?: "No data"
            Log.d("BLE", "Data received: $receivedData")
            onDataReceived?.invoke(receivedData)
        }
    }
}