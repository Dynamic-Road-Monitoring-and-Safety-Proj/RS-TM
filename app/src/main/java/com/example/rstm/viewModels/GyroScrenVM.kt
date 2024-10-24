package com.example.rstm.viewModels

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class GyroViewModel : ViewModel() {
    var gyroscope: Sensor? = null
    var x1 = mutableFloatStateOf(0f)
    var y1 = mutableFloatStateOf(0f)
    var z1 = mutableFloatStateOf(0f)


    private val gyroEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    x1.floatValue = event.values[0]
                    y1.floatValue = event.values[1]
                    z1.floatValue = event.values[2]

//                    values[0]: Angular speed around the x-axis
//                    values[1]: Angular speed around the y-axis
//                    values[2]: Angular speed around the z-axis
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Handle accuracy changes if needed
        }
    }

    fun startGyroSensor(sensorManager: SensorManager) {
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(
            gyroEventListener,
            gyroscope,
            SensorManager.SENSOR_DELAY_NORMAL
        )}
    fun stopGyroSensor(sensorManager: SensorManager){
        sensorManager.unregisterListener(gyroEventListener)
    }
}