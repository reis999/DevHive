package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp

/**
 * Modelo de domínio para representar um material de estudo
 */
data class Material(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val contentUrl: String,
    val thumbnailUrl: String,
    val fileSize: Long,
    val ownerUid: String,
    val ownerName: String,
    val ownerImageUrl: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val categories: List<String>,
    val isPublic: Boolean,
    val subject: String,
    val views: Int,
    val downloads: Int,
    val likes: Int,
    val bookmarks: Int,
    val bookmarkedBy: List<String>,
    val likedBy: List<String>,
    val rating: Float,
    val reviewCount: Int
)
