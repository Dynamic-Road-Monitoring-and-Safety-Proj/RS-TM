package com.example.rstm.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CameraScreen() {
    Column {
        Button(onClick = { /*TODO StartRecording */ }) {
            Text(text = "Record For 15 Seconds")
        }

    }
}