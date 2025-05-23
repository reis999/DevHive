package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val imageUrl: String = "",
    val members: List<String> = emptyList(),
    val admins: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val isPrivate: Boolean = false,
    val joinCode: String = "",
    val maxMembers: Int = 100,
    val lastMessageAt: Timestamp? = null,
    val lastMessagePreview: String = "",
    val messageCount: Int = 0
)
