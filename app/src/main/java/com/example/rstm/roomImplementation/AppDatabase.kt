package com.example.rstm.roomImplementation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rstm.roomImplementation.RoomDao
import com.example.rstm.roomImplementation.RoomEntity

@Database(entities = [RoomEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val NAME = "room.db"
    }
    abstract fun getDao() : RoomDao

}
