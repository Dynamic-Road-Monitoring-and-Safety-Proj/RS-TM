package com.example.rstm.roomImplementation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uri_table")
data class RoomEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "video_uri") val videoUri: String?,
    @ColumnInfo(name = "accelerometer_uri") val accelerometerUri: String?,
    @ColumnInfo(name = "gyro_uri") val gyroUri: String?,
    @ColumnInfo(name = "location_uri") val locationUri: String?,
    @ColumnInfo(name = "light_uri") val light_Uri: String?,
    @ColumnInfo(name = "time_uri") val time_Uri: String?,
){

}