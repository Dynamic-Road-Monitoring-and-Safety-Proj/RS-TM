package com.example.rstm.ui.screens

import android.hardware.Sensor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

@Composable
fun gyroscopeScreen(
    modifier: Modifier,
    gyro: Sensor?,
    x: MutableState<Float>,
    y: MutableState<Float>,
    z: MutableState<Float>
) {
    Column(modifier) {
        Text(text = "Gyroscope Screen", modifier = modifier.fillMaxWidth())
        if (gyro != null) {
            Text(text = "Gyroscope is available")
        } else {
            Text(text = "Gyroscope is not available")
        }
        Text(text = "x: ${x.value}")
        Text(text = "y: ${y.value}")
        Text(text = "z: ${z.value}")
    }
}
