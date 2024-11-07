package com.example.rstm.roomImplementation

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [RoomEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val NAME = "room.db"
    }
    abstract fun getDao() : RoomDao

}
