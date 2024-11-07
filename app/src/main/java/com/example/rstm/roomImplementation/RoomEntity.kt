package com.example.rstm.roomImplementation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uri_table")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "video_uri_list") val videoUriList: List<String>?, // Changed to List<String>
    @ColumnInfo(name = "csv_uri") val csvUri: String?  // Changed to String
)
