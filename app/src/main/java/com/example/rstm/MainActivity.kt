package com.example.rstm


import AccelerometerScreen
import GyroscopeScreen
import LightScreenComp
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rstm.ui.screens.CameraScreen
import com.example.rstm.ui.screens.HomeScreen
import com.example.rstm.ui.screens.LocationScreen
import com.example.rstm.ui.screens.magFieldScreen
import com.example.rstm.ui.theme.RSTMTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val PermissionArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { PermissionsList ->
            PermissionsList.entries.forEach{isGranted->
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
        val permissionsToRequest = PermissionArray.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            Toast.makeText(this, "All permissions are already granted", Toast.LENGTH_SHORT).show()
        }
    }
    //TODO MAKE ABOVE LOCATION PART MODULAR after sensors are done
    private lateinit var sensorManager: SensorManager


    var magField: Sensor? = null
    var x2 = mutableStateOf(0f)
    var y2 = mutableStateOf(0f)
    var z2 = mutableStateOf(0f)

    private val magFieldEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    x2.value = event.values[0]
                    y2.value = event.values[1]
                    z2.value = event.values[2]
//                    ambient magnetic field in the X, Y and Z axis.
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkPermission()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        enableEdgeToEdge()
           setContent {
            val navController = rememberNavController()
            RSTMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(Modifier.padding(innerPadding),navController)
                        }
                        composable("accelerometer"){
                            AccelerometerScreen(Modifier.padding(innerPadding))//
                        }
                        composable("gyro"){
                            GyroscopeScreen(modifier = Modifier.padding(innerPadding),sensorManager)
                        }
                        composable("magField"){
                            magFieldScreen(modifier = Modifier.padding(innerPadding), magField = magField, x = x2, y = y2, z = z2)
                        }
                        composable("lightScreen"){
                            LightScreenComp(modifier = Modifier.padding(innerPadding), sensorManager)
                        }
                        composable("locationScreen"){
                            LocationScreen(modifier = Modifier.padding(innerPadding), fusedLocationClient)
                        }
                        composable("cameraScreen"){
                            CameraScreen(Modifier, this@MainActivity)
                        }
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            magFieldEventListener,
            magField,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(magFieldEventListener)
    }
}

@Preview
@Composable
fun DefaultPreview() {
    RSTMTheme {
//        HomeScreen(Modifier.padding(16.dp), NavController())
    }
}