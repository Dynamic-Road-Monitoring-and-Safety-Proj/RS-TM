package com.example.rstm.model

import android.location.Location
import java.sql.Timestamp

// Data class to store sensor data similar to a state class as this is the point of absolute truth for the collective VMs.

data class SensorData(
    var accelerometerData: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    var magneticData : Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    var gyroscopeData: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    var lightData:  Float = 0f,
    var locationData: Location = Location(""),
    var timestamp: Timestamp = Timestamp(System.currentTimeMillis())
    // while making list, time stamp will be added to the data as pairs of timestamp and SensorData object
)