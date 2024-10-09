package com.example.rstm.roomImplementation

import android.content.ClipData.Item
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: RoomEntity)

    @Update
    suspend fun update(item: RoomEntity)

    @Delete
    suspend fun delete(item: RoomEntity)

    @Query("SELECT * from uri_table WHERE id = :id")
    fun getTable(id: Int): Flow<RoomEntity>
}
