package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "",
    val participant1Id: String = "",
    val participant2Id: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val lastMessageAt: Timestamp = Timestamp.now(),
    val lastMessagePreview: String = "",
    val messageCount: Int = 0,
    val otherParticipantId: String = "",
    val otherParticipantName: String = "",
    val otherParticipantImageUrl: String = "",
    val otherParticipantOnline: Boolean = false,
    val unreadCount: Int = 0
)

