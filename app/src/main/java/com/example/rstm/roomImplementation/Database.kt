import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rstm.roomImplementation.RoomDao
import com.example.rstm.roomImplementation.RoomEntity

@Database(entities = [RoomEntity::class], version = 2, exportSchema = false)
@TypeConverters(UriConverters::class) // Ensure this is added if using converters
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()  // Optional: handle migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
