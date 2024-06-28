package com.example.rstm


import AccelerometerScreen
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rstm.ui.screens.HomeScreen
import com.example.rstm.ui.screens.gyroscopeScreen
import com.example.rstm.ui.theme.RSTMTheme

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    var accelerometer: Sensor? = null
    var x = mutableStateOf(0f)
    var y = mutableStateOf(0f)
    var z = mutableStateOf(0f)

    var gyroscope: Sensor? = null
    var x1 = mutableStateOf(0f)
    var y1 = mutableStateOf(0f)
    var z1 = mutableStateOf(0f)

    var magneticField: Sensor? = null
    var x2 = mutableStateOf(0f)
    var y2 = mutableStateOf(0f)
    var z2 = mutableStateOf(0f)


    private val accEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    x.value = event.values[0]
                    y.value = event.values[1]
                    z.value = event.values[2]
//                    values[0]: Acceleration minus Gx on the x-axis
//                    values[1]: Acceleration minus Gy on the y-axis
//                    values[2]: Acceleration minus Gz on the z-axis
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
    private val gyroEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    x1.value = event.values[0]
                    y1.value = event.values[1]
                    z1.value = event.values[2]
//                    values[0]: Angular speed around the x-axis
//                    values[1]: Angular speed around the y-axis
//                    values[2]: Angular speed around the z-axis
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
    private val magfieldEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
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


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Toast.makeText(this, "Accelerometer Activity", Toast.LENGTH_SHORT).show()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null){
            Toast.makeText(this, "Accelerometer is not available", Toast.LENGTH_SHORT).show()
        }
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
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
                            AccelerometerScreen(Modifier.padding(innerPadding), accelerometer, x, y, z)//
                        }
                        composable("gyro"){
                            gyroscopeScreen(modifier = Modifier.padding(innerPadding), gyro = gyroscope, x = x1, y = y1, z = z1)
                        }
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            accEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this.gyroEventListener,
            gyroscope,
            SensorManager.SENSOR_DELAY_NORMAL

        )
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(accEventListener)
        sensorManager.unregisterListener(this.gyroEventListener)
    }
}

@Preview
@Composable
fun DefaultPreview() {
    RSTMTheme {
//        HomeScreen(Modifier.padding(16.dp))
    }
}