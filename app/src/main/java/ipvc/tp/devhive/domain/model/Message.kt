package ipvc.tp.devhive.domain.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

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

@Parcelize
data class MessageAttachment(
    val id: String,
    val name: String,
    val type: String,
    val url: String,
    val size: Long,
    val fileExtension: String
): Parcelable
