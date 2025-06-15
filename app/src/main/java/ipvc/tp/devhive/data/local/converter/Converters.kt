package ipvc.tp.devhive.data.local.converter

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
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

class TimestampConverter {
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it / 1000, ((it % 1000) * 1000000).toInt()) }
    }
}
