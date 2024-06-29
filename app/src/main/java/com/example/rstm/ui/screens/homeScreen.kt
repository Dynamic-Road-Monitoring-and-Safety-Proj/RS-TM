package com.example.rstm.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier,navController:NavController) {
    Column(modifier){
        TopAppBar(title = { Text(text = "RSTM"    )})
        Button(onClick = {
            navController.navigate("accelerometer")
                         },) {
            Text(text = "Accelerometer Screen")
        }
        Button(onClick = {
            navController.navigate("gyro")
        },) {
            Text(text = "Gyroscope Screen")
        }
        Button(onClick = {
            navController.navigate("magField")
        },) {
            Text(text = "Magnetic Field/Compass")
        }
        Button(onClick = {
            navController.navigate("lightScreen")
        },) {
            Text(text = "Environment Light")
        }
    }
}