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
            val fragmentActivity = context as? FragmentActivity
                ?: throw IllegalStateException("Context is not a FragmentActivity.")

            FragmentContainerView(fragmentActivity).apply {
                id = View.generateViewId()
                val fragment = yoloFragment()
                fragmentActivity.supportFragmentManager.beginTransaction()
                    .add(id, fragment)
                    .commitNow()

                // Add cleanup on dispose
                addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {}
                    override fun onViewDetachedFromWindow(v: View) {
                        fragmentActivity.supportFragmentManager.beginTransaction()
                            .remove(fragment)
                            .commitNow()
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}