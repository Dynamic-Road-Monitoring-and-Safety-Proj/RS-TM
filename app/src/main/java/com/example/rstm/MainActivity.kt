package com.example.rstm


import AccelerometerScreen
import GyroscopeScreen
import ImplementRepository
import ImplementScreen
import LightScreenComp
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.rstm.model.SensorData
import com.example.rstm.ui.screens.Activated
import com.example.rstm.ui.screens.CameraPreviewScreen
import com.example.rstm.ui.screens.CameraScreen
import com.example.rstm.ui.screens.HomeScreen
import com.example.rstm.ui.screens.LocationScreen
import com.example.rstm.ui.screens.MagFieldScreen
import com.example.rstm.ui.theme.RSTMTheme
import com.example.rstm.viewModels.ImplementVM
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.rstm.roomImplementation.AppDatabase
import java.io.IOException
import java.sql.Timestamp
import java.util.UUID


class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager

    companion object{
        lateinit var appDatabase: AppDatabase
        const val REQUEST_CODE_STORAGE_PERMISSION = 101
    }

    // Permissions required for the app
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionArray = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsList ->
            permissionsList.entries.forEach { isGranted ->
                if (isGranted.value) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Permission is required to access Sensors and Files, Enable it in device settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission() {
        val permissionsToRequest = permissionArray.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            checkExternalStoragePermission()
        }
    }
    private var bluetoothAdapter: BluetoothAdapter? = null
    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {
        val NAME = "BluetoothChatService"
        val MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetooth() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)!!
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        } else if (bluetoothAdapter!!.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val REQUEST_ENABLE_BT = 1
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
        }
    }
    // Request external storage permission
    private fun checkExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestStoragePermission()
            } else {
                Toast.makeText(this, "Manage external storage permission granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                Toast.makeText(this, "Write external storage permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Request permission to manage all files (Android 11+)
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    // Initialize sensor Data class
    val sensorData = SensorData()

    fun changeGyroData(x: Float, y: Float, z: Float) { //also time
        sensorData.gyroscopeData = Triple(x, y, z)
        sensorData.timestamp = Timestamp(System.currentTimeMillis())
    }
    fun changeAccData(x: Float, y: Float, z: Float) {
        sensorData.accelerometerData = Triple(x, y, z)
    }
    fun changeMagData(x: Float, y: Float, z: Float) {
        sensorData.magneticData = Triple(x, y, z)
    }
    fun changeLightData(light: Float) {
        sensorData.lightData = light
    }
    fun changeLocationData(location: android.location.Location) {
        sensorData.locationData = location
    }

    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action.toString()
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkBluetooth()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        appDatabase = Room.databaseBuilder(applicationContext, AppDatabase::class.java, AppDatabase.NAME).build()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize sensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        checkPermission()

        val implementVM = ImplementVM(sensorManager, fusedLocationClient, ImplementRepository())

        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            RSTMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(Modifier.padding(innerPadding), navController)
                        }
                        composable("accelerometer") {
                            AccelerometerScreen(Modifier.padding(innerPadding), sensorManager, ::changeAccData)
                        }
                        composable("gyro") {
                            GyroscopeScreen(modifier = Modifier.padding(innerPadding), sensorManager, ::changeGyroData)
                        }
                        composable("magField") {
                            MagFieldScreen(modifier = Modifier.padding(innerPadding), sensorManager, ::changeMagData)
                        }
                        composable("lightScreen") {
                            LightScreenComp(modifier = Modifier.padding(innerPadding), sensorManager, :: changeLightData)
                        }
                        composable("locationScreen") {
                            LocationScreen(modifier = Modifier.padding(innerPadding), fusedLocationClient, ::changeLocationData)
                        }
                        composable("cameraScreen") {
                            CameraScreen(Modifier, this@MainActivity, applicationContext)
                        }
                        composable("Detection & Collection Activated") {
                            Activated(Modifier.padding(innerPadding), sensorManager, this@MainActivity, applicationContext, fusedLocationClient)
                        }
                        composable("Hidden View") {
                            CameraPreviewScreen(Modifier = Modifier.padding(innerPadding), sensorManager = sensorManager, fusedLocationClient = fusedLocationClient)
                        }
                        composable("Implement Screen"){
                            ImplementScreen(viewModel = implementVM, Modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }
}

@Preview
@Composable
fun DefaultPreview() {
    RSTMTheme {

    }
}