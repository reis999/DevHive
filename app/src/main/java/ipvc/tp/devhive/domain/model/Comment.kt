package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp

/**
 * Modelo de domínio para representar um comentário
 */
data class Comment(
    val id: String,
    val materialId: String,
    val userUid: String,
    val content: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val likes: Int,
    val parentCommentId: String?,
    val attachments: List<Attachment>
)

data class Attachment(
    val type: String,
    val url: String
)
