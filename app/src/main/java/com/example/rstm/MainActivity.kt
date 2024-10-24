package com.example.rstm


import AccelerometerScreen
import GyroscopeScreen
import ImplementRepository
import ImplementScreen
import LightScreenComp
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
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
        Manifest.permission.RECORD_AUDIO
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

    fun changeGyroData(x: Float, y: Float, z: Float) {
        sensorData.gyroscopeData = Triple(x, y, z)
    }
    fun changeAccData(x: Float, y: Float, z: Float) {
        sensorData.accelerometerData = Triple(x, y, z)
    }
    fun changeLightData(light: Float) {
        sensorData.lightData = light
    }
    fun changeLocationData(location: android.location.Location) {
        sensorData.locationData = location
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDatabase = Room.databaseBuilder(applicationContext, AppDatabase::class.java, AppDatabase.NAME).build()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize sensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        checkPermission()

        val implementVM = ImplementVM(sensorManager, fusedLocationClient, ImplementRepository(applicationContext))

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
                            AccelerometerScreen(Modifier.padding(innerPadding), sensorManager, changeAccData)
                        }
                        composable("gyro") {
                            GyroscopeScreen(modifier = Modifier.padding(innerPadding), sensorManager)
                        }
                        composable("magField") {
                            MagFieldScreen(modifier = Modifier.padding(innerPadding), sensorManager)
                        }
                        composable("lightScreen") {
                            LightScreenComp(modifier = Modifier.padding(innerPadding), sensorManager)
                        }
                        composable("locationScreen") {
                            LocationScreen(modifier = Modifier.padding(innerPadding), fusedLocationClient)
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
}

@Preview
@Composable
fun DefaultPreview() {
    RSTMTheme {

    }
}