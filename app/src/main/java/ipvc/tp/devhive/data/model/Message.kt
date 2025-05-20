package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.util.SyncStatus

data class Message(
    val id: String = "",
    val chatId: String = "",
    val content: String = "",
    val senderUid: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val attachments: List<MessageAttachment> = emptyList(),
    val syncStatus: String = SyncStatus.SYNCED,
    val lastSync: Timestamp = Timestamp.now()
)

data class MessageAttachment(
    val type: String = "",
    val url: String = "",
    val name: String = "",
    val size: Long = 0
)
