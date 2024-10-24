package com.example.rstm.roomImplementation

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    // Convert Uri to String
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    // Convert String to Uri
    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }

    // Convert List<Uri> to String (JSON)
    @TypeConverter
    fun fromUriList(uriList: List<Uri>?): String? {
        return gson.toJson(uriList)
    }

    // Convert String (JSON) to List<Uri>
    @TypeConverter
    fun toUriList(uriListString: String?): List<Uri>? {
        return if (uriListString.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<Uri>>() {}.type
            gson.fromJson(uriListString, type)
        }
    }
}
