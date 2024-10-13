import androidx.room.TypeConverter
import android.net.Uri

class UriConverters {
    @TypeConverter
    fun fromUriList(uriList: List<Uri>): String {
        return uriList.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toUriList(data: String): List<Uri> {
        return data.split(",").map { Uri.parse(it) }
    }
}