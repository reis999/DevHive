package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp

data class Chat(
    val id: String,
    val participant1Id: String,
    val participant1Name: String,
    val participant2Id: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val lastMessageAt: Timestamp,
    val lastMessagePreview: String,
    val messageCount: Int,
    val otherParticipantId: String = "",
    val otherParticipantName: String = "",
    val otherParticipantImageUrl: String = "",
    val otherParticipantOnline: Boolean = false,
    val unreadCount: Int = 0
)

