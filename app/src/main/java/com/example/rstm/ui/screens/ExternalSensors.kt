package com.example.rstm.ui.screens

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager

class ExternalSensors {
    val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
    if (bluetoothAdapter == null) {
        // Device doesn't support Bluetooth
    }
}