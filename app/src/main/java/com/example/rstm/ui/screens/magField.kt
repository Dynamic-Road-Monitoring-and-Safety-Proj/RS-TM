package com.example.rstm.ui.screens


import android.hardware.Sensor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

@Composable
fun magFieldScreen(
    modifier: Modifier,
    magField: Sensor?,
    x: MutableState<Float>,
    y: MutableState<Float>,
    z: MutableState<Float>
) {
    Column(modifier) {
        Text(text = "MagField Screen", modifier = modifier.fillMaxWidth())
        if (magField != null) {
            Text(text = "MagField is available")
        } else {
            Text(text = "MagField is not available")
        }
        Text(text = "x: ${x.value}")
        Text(text = "y: ${y.value}")
        Text(text = "z: ${z.value}")
    }
}
