package com.example.rstm.ui.screens

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.android.gms.location.*
import kotlinx.coroutines.delay

@Composable
fun LocationScreen(
    modifier: Modifier = Modifier,
    fusedLocationClient: FusedLocationProviderClient,
    function: (Location) -> Unit
) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current


    // Check and request permissions if necessary
    LaunchedEffect(lifecycleOwner) {
        while (true) {
            fetchLocation(fusedLocationClient) { location, error ->
                currentLocation = location
                errorMessage = error
            }
            delay(5000)
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
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocation(
    fusedLocationClient: FusedLocationProviderClient,
    callback: (Location?, String?) -> Unit
) {
    val locationRequest: CurrentLocationRequest = CurrentLocationRequest.Builder()
        .setDurationMillis(500)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setGranularity(Granularity.GRANULARITY_FINE)
        .build()

    fusedLocationClient.getCurrentLocation(locationRequest, null)
        .addOnSuccessListener { location: Location? ->
            Log.d("LocationScreen", "Location fetched: $location")
            if (location != null) {
                callback(location, null)
            } else {
                // Fallback to request location updates
                val request = LocationRequest.create().apply {
                    interval = 2000
                    fastestInterval = 1000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationClient.requestLocationUpdates(
                    request,
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val loc = locationResult.lastLocation
                            if (loc != null) {
                                callback(loc, null)
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        }
                    },
                    null
                )
            }
        }
        .addOnFailureListener { e ->
            Log.e("LocationScreen", "Failed to get location", e)
            callback(null, "Failed to get location")
        }
}
