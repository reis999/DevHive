package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp

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
    val read: Boolean = false
)

data class MessageAttachment(
    val type: String,
    val url: String,
    val name: String,
    val size: Long
)
