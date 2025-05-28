package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.model.MessageAttachment

/**
 * Modelo de domínio para representar uma mensagem
 */
data class Message(
    val id: String,
    val chatId: String,
    val content: String,
    val senderUid: String,
    val createdAt: Timestamp,
    val attachments: List<MessageAttachment>,
    val read: Boolean,
    val syncStatus: String,
    val lastSync: Timestamp
)

data class MessageAttachment(
    val type: String,
    val url: String,
    val name: String,
    val size: Long
)
