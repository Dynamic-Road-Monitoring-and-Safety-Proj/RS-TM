

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import com.example.rstm.roomImplementation.RoomDao
import com.example.rstm.roomImplementation.RoomEntity

@Database(entities = [RoomEntity::class], version = 1)   // TODO HOW TO STATIFY THIS VERSION
abstract class Database : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    companion object {
        // Volatile ensures visibility of changes to other threads
        @Volatile
        private var INSTANCE: Database? = null

        // Singleton pattern to ensure only one instance of the database is created
        fun getDatabase(context: Context): Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Database::class.java,
                    "my_database"  // The name of your database
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}