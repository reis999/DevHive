package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val materialId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImageUrl: String = "",
    val content: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val liked: Boolean = false,
    val parentCommentId: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val lastSync: Timestamp = Timestamp.now()
)

data class Attachment(
    val type: String = "",
    val url: String = ""
)

