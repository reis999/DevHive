package ipvc.tp.devhive.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ipvc.tp.devhive.data.model.Attachment
import ipvc.tp.devhive.data.model.MessageAttachment

class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }
}

class AttachmentListConverter {
    @TypeConverter
    fun fromString(value: String): List<Attachment> {
        val listType = object : TypeToken<List<Attachment>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Attachment>): String {
        return Gson().toJson(list)
    }
}

class MessageAttachmentListConverter {
    @TypeConverter
    fun fromString(value: String): List<MessageAttachment> {
        val listType = object : TypeToken<List<MessageAttachment>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<MessageAttachment>): String {
        return Gson().toJson(list)
    }
}

class MapStringAnyConverter {
    @TypeConverter
    fun fromString(value: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Any>): String {
        return Gson().toJson(map)
    }
}
