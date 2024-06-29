package com.example.rstm.ui.screens

import android.annotation.SuppressLint
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
@Composable
fun LocationScreen(modifier: Modifier = Modifier, fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val locationRequest: CurrentLocationRequest = CurrentLocationRequest.Builder()
            .setDurationMillis(TimeUnit.SECONDS.toMillis(2))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .build()

        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location: Location? ->
                currentLocation = location
            }
            .addOnFailureListener {
                errorMessage = "Failed to get location"
            }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Location Screen", modifier = modifier)
        currentLocation?.let {
            Text(text = "Latitude: ${it.latitude}")
            Text(text = "Longitude: ${it.longitude}")
            Text(text = "Altitude: ${it.altitude}")
            Text(text = "Speed: ${it.speed}")
            Text(text = "Accuracy: ${it.accuracy}")
            Text(text = "Provider: ${it.provider}")
        } ?: run {
            Text(text = "Fetching location...")
        }

        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null // Reset the error message after showing it
        }
    }
}
