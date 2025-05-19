package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class Material(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "",
    val contentUrl: String = "",
    val thumbnailUrl: String = "",
    val fileSize: Long = 0,
    val ownerUid: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val categories: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val subject: String = "",
    val views: Int = 0,
    val likes: Int = 0,
    val downloads: Int = 0,
    val bookmarked: Boolean = false,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val lastSync: Timestamp = Timestamp.now()
)
