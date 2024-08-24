package com.example.rstm


import AccelerometerScreen
import GyroscopeScreen
import LightScreenComp
import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rstm.ui.screens.Activated
import com.example.rstm.ui.screens.CameraPreviewScreen
import com.example.rstm.ui.screens.CameraScreen
import com.example.rstm.ui.screens.HomeScreen
import com.example.rstm.ui.screens.LocationScreen
import com.example.rstm.ui.screens.MagFieldScreen
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkPermission()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

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
                            AccelerometerScreen(Modifier.padding(innerPadding), sensorManager)//
                        }
                        composable("gyro"){
                            GyroscopeScreen(modifier = Modifier.padding(innerPadding),sensorManager)
                        }
                        composable("magField"){
                            MagFieldScreen(modifier = Modifier.padding(innerPadding), sensorManager)
                        }
                        composable("lightScreen"){
                            LightScreenComp(modifier = Modifier.padding(innerPadding), sensorManager)
                        }
                        composable("locationScreen"){
                            LocationScreen(modifier = Modifier.padding(innerPadding), fusedLocationClient)
                        }
                        composable("cameraScreen"){
                            CameraScreen(Modifier, this@MainActivity, applicationContext)
                        }
                        composable("Detection & Collection Activated"){
                            Activated(Modifier.padding(innerPadding), sensorManager, this@MainActivity ,applicationContext, fusedLocationClient)
                        }
                        composable("Hidden View"){
                            CameraPreviewScreen(Modifier = Modifier.padding(innerPadding), sensorManager = sensorManager,  fusedLocationClient = fusedLocationClient)
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