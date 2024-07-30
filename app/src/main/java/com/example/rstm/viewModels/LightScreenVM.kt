package com.example.rstm.viewModels

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class LightViewModel : ViewModel() {
     var lightSensor: Sensor? = null
    val lightValue: MutableState<Float> = mutableStateOf(0f)
    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_LIGHT) {
                    lightValue.value = it.values[0]
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Handle accuracy changes if needed
        }
    }
    fun startLightSensor(sensorManager: SensorManager) {
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    fun stopLightSensor(sensorManager: SensorManager){
        sensorManager.unregisterListener(lightListener)
    }
}

