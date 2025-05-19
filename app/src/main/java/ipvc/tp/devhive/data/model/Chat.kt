package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "",
    val name: String = "",
    val isPrivate: Boolean = false,
    val accessCode: String = "",
    val maxCapacity: Int = 50,
    val participantIds: List<String> = emptyList(),
    val creatorUid: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val lastMessageAt: Timestamp = Timestamp.now(),
    val lastMessagePreview: String = "",
    val messageCount: Int = 0,
    val lastSync: Timestamp = Timestamp.now()
)
