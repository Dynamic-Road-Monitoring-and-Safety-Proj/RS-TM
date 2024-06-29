//package com.example.rstm.ui.screens
//
//import android.app.Activity
//import android.content.Context.SENSOR_SERVICE
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import android.hardware.Sensor;
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.runtime.Composable;
//import androidx.compose.runtime.MutableState;
//import androidx.compose.material3.Text
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.ui.Modifier
//import androidx.core.content.ContextCompat.getSystemService
//import androidx.fragment.app.Fragment
//import com.example.rstm.MainActivity
//
//class lightScreen:ComponentActivity() {
//    var lightS: Sensor? = null
//    var light = mutableStateOf(0f)
//    val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//
//    private val lightListener = object : SensorEventListener {
//        override fun onSensorChanged(event: SensorEvent?) {
//            if (event != null) {
//                if (event.sensor.type == Sensor.TYPE_LIGHT) {
//                    light.value = event.values[0]
//                    // values[0]: Ambient light level in SI lux units
//                }
//            }
//        }
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        lightS = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
//    }
//    override fun onResume() {
//        super.onResume()
//        sensorManager.registerListener(
//            lightListener,
//            lightS,
//            SensorManager.SENSOR_DELAY_NORMAL
//        )
//    }
//    override fun onPause() {
//        super.onPause()
//        sensorManager.unregisterListener(lightListener)
//    }
//    fun getLightVal(): MutableState<Float> {
//        return light
//    }
//
//    fun getLightSensor(): Sensor? {
//        return lightS
//    }
//}
//
//@Composable
//fun lightScreenComp(
//    modifier:Modifier
//) {
//    val activity = lightScreen()
//    val light = activity.getLightVal()
//    val lightS = activity.getLightSensor()
//    Column(modifier) {
//        Text(text = "Light brightness Screen", modifier = modifier.fillMaxWidth())
//        if (lightS != null) {
//            Text(text = "LightS is available")
//        } else {
//            Text(text = "LightS is not available")
//        }
//        Text(text = "x: ${light.value}")
//    }
//}