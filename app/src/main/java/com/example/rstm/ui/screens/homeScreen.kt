package com.example.rstm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "RSTM", fontSize = 20.sp, color = Color.White) },
                Modifier.background(MaterialTheme.colorScheme.onPrimary),
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "NOT YET IMPLEMENTED", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    navController.navigate("accelerometer")
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Accelerometer Screen")
            }
            Button(
                onClick = {
                    navController.navigate("gyro")
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Gyroscope Screen")
            }
            Button(
                onClick = {
                    navController.navigate("magField")
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Magnetic Field/Compass")
            }
            Button(
                onClick = {
                    navController.navigate("lightScreen")
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Environment Light")
            }
            Button(
                onClick = {
                    navController.navigate("locationScreen")
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Location*-")
            }
            Button(
                onClick = {
                    navController.navigate("cameraScreen")
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Camera Screen")
            }
//            Button(
//                onClick = { navController.navigate("Detection & Collection Activated") },
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text(text = "Active Screen")
//            }
            Button(
                onClick = { navController.navigate("Hidden View") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Hidden Active")
            }
            Button(
                onClick = { navController.navigate("Implement Screen") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Implement Screen")
            }
            Button(
                onClick = { navController.navigate("BLE") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Bluetooth Screen")
            }
            Button(
                onClick = { navController.navigate("yoloFragment") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "yolo Screen")
            }
        }
    }
}
