package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp

data class StudyGroup(
    val id: String,
    val name: String,
    val description: String,
    val createdBy: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val imageUrl: String,
    val members: List<String>,
    val admins: List<String>,
    val categories: List<String>,
    val isPrivate: Boolean,
    val joinCode: String,
    val maxMembers: Int = 100,
    val lastMessageAt: Timestamp? = null,
    val lastMessagePreview: String = "",
    val messageCount: Int = 0,
    val unreadCount: Int = 0
)

