package com.example.rstm.roomImplementation

import androidx.room.RoomDatabase
import androidx.room.Database

@Database(entities = [RoomEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun roomDao(): RoomDao
}