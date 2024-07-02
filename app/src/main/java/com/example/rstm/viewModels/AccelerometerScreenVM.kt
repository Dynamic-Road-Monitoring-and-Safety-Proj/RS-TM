package com.example.rstm.viewModels

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AccelerometerScreenVM : ViewModel() {
    var accelerometer: Sensor? = null
    var x = mutableStateOf(0f)
    var y = mutableStateOf(0f)
    var z = mutableStateOf(0f)
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
    fun startAccelerometer(sensorManager: SensorManager) {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(accEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }
    fun stopAccelerometer(sensorManager: SensorManager){
        sensorManager.unregisterListener(accEventListener)
    }

}