package com.example.rstm.viewModels

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MagneticFieldScreenVM: ViewModel() {
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
    fun startMagField(sensorManager: SensorManager){
        magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(
            magFieldEventListener,
            magField,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
    fun stopMagField(sensorManager: SensorManager){
        sensorManager.unregisterListener(magFieldEventListener)
    }
}