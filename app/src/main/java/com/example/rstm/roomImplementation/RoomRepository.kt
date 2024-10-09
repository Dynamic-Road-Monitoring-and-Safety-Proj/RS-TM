package com.example.rstm.roomImplementation

import android.net.Uri

class RoomRepository(private val roomDao: RoomDao) {

    // Insert or update the list of URIs
    suspend fun insertUriList(uris: List<Uri>) {
        val uriStrings = uris.map { it.toString() }  // Convert URIs to Strings
        val roomEntity = RoomEntity(id = 1, videoUriList = uriStrings)  // Assuming one entity with ID 1 for now
        roomDao.insertUriList(roomEntity)
    }

    // Fetch the URI list from the database
    fun getUriList(): LiveData<List<Uri>> {
        val liveData = MutableLiveData<List<Uri>>()

        // Get the entity from the database
        roomDao.getUriList(1).collect { roomEntity ->
            roomEntity?.let {
                val uriList = it.videoUriList?.map { uriString -> Uri.parse(uriString) } ?: emptyList()
                liveData.postValue(uriList)
            }
        }

        return liveData
    }
}
