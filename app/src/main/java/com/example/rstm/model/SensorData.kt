package com.example.rstm.model

import android.location.Location

// Data class to store sensor data similar to a state class as this is the point of absolute truth for the collective VMs.

data class SensorData(
    val accelerometerData: Triple<Float, Float, Float>,
    val gyroscopeData: Triple<Float, Float, Float>,
    val lightData:  Float,
    val locationData: Location,
    val timestamp : Long
)