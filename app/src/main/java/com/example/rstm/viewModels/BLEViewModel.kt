package com.example.rstm.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BLEViewModel : ViewModel() {
    private val _receivedData = MutableStateFlow("")
    val receivedData: StateFlow<String> get() = _receivedData

    fun updateData(data: String) {
        _receivedData.value = data
    }
}
