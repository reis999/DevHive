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
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val categories: List<String>,
    val isPublic: Boolean,
    val subject: String,
    val views: Int,
    val likes: Int,
    val downloads: Int,
    val bookmarked: Boolean,
    val rating: Float,
    val reviewCount: Int
)
