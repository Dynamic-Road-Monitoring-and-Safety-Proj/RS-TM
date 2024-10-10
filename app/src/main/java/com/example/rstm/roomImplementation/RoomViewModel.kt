package com.example.rstm.roomImplementation

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RoomViewModel(private val repository: RoomRepository) : ViewModel() {

    // LiveData to observe URIs
    val uriList: LiveData<List<Uri>> = repository.getTable()

    // Function to insert the new list of URIs
    fun insertUriList(uris: List<Uri>) {
        viewModelScope.launch {
            repository.insertUriList(uris)
        }
    }

    // Function to add a new URI to the existing list
    fun addUri(newUri: Uri) {
        val currentList = uriList.value?.toMutableList() ?: mutableListOf()
        currentList.add(newUri)
        insertUriList(currentList)
    }
}
