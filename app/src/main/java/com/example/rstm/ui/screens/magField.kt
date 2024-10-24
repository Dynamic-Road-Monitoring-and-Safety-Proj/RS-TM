package com.example.rstm.ui.screens


import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rstm.viewModels.MagneticFieldScreenVM

@Composable
fun MagFieldScreen(
    modifier: Modifier,
    sensorManager: SensorManager,
    function: (Float, Float, Float) -> Unit
) {
    val magVM : MagneticFieldScreenVM = viewModel()
    DisposableEffect(sensorManager) {
        magVM.startMagField(sensorManager)
        onDispose {
            magVM.stopMagField(sensorManager)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            function(magVM.x2.value, magVM.y2.value, magVM.z2.value)
        }
    }
    Column(modifier) {
        Text(text = "MagField Screen", modifier = modifier.fillMaxWidth())
        if (magVM.magField != null) {
            Text(text = "MagField is available")
        } else {
            Text(text = "MagField is not available")
        }
        Text(text = "x: ${magVM.x2.value}")
        Text(text = "y: ${magVM.y2.value}")
        Text(text = "z: ${magVM.z2.value}")
    }
}
