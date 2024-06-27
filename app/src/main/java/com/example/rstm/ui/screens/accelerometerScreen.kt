package com.example.rstm.ui.screens

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

class accActivity(): ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        /* Context class has the sensor service constant declared while normal context
        does not have it,keeping it blank will take context by defaults */
        accelerometer = if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // Success! There's an accelerometer.
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else {
            // Failure! No accelerometer.
            null
        }
    }

    fun getAccelerometer(): Sensor? {
        return accelerometer
    }
}


@Composable
fun AccelerometerScreen(modifier: Modifier) {
    val accelerometer = accActivity().getAccelerometer()
    Column(modifier) {
        Text(text = "Accelerometer Screen")
        if (accelerometer != null) {
            Text(text = "Accelerometer is available")
        } else {
            Text(text = "Accelerometer is not available")
        }
    }
}