package com.example.rstm.model

import android.location.Location

data class SensorData(
    val accelerometerData: MutableList<Pair<Long,Triple<Float, Float, Float>>> = mutableListOf(),  // timestamp, x, y, z
    val gyroscopeData: MutableList<Pair<Long,Triple<Float, Float, Float>>> = mutableListOf(),
    val lightData: MutableList<Pair<Long, Float>> = mutableListOf(),  // timestamp, light value
    val locationData: MutableList<Pair<Long, Location>> = mutableListOf()
)