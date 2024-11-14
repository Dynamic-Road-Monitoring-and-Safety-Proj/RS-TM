package com.example.rstm.roomImplementation

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uri_table")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "video_uri_file") val videoUriFile: String,  // Holds the name of the CSV file
    @ColumnInfo(name = "csv_uri") val csvUri: Uri?
)
