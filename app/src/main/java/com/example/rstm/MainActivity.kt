package com.example.rstm


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rstm.ui.screens.HomeScreen
import com.example.rstm.ui.theme.RSTMTheme

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    var x = mutableStateOf(0f)
    var y = mutableStateOf(0f)
    var z = mutableStateOf(0f)
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    x.value = event.values[0]
                    y.value = event.values[1]
                    z.value = event.values[2]
                    // Do something with x, y, and z
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
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    @Composable
    fun AccelerometerScreen(modifier: Modifier) {

        Column(modifier) {
            Text(text = "Accelerometer Screen", modifier = modifier.fillMaxWidth())
            if (accelerometer != null) {
                Text(text = "Accelerometer is available")
            } else {
                Text(text = "Accelerometer is not available")
            }
            Text(text = "x: ${x.value}")
            Text(text = "y: ${y.value}")
            Text(text = "z: ${z.value}")
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    RSTMTheme {
//        HomeScreen(Modifier.padding(16.dp))
    }
}