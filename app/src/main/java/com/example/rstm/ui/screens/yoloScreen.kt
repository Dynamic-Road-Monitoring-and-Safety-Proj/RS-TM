package com.example.rstm.ui.screens

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentActivity
import com.example.rstm.yolo.yoloFragment

@Composable
fun YoloFragmentScreen() {
    AndroidView(
        factory = { context ->
            // Ensure the context is a FragmentActivity
            val fragmentActivity = context as FragmentActivity
            val fragment = yoloFragment()
            FragmentContainerView(context).apply {
                id = View.generateViewId()
                fragmentActivity.supportFragmentManager.beginTransaction()
                    .add(id, fragment)
                    .commitNow() // Use commitNow to ensure it's immediately added
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
