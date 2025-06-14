package ipvc.tp.devhive.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.parcelize.Parcelize

data class Message(
    val id: String = "",
    val chatId: String = "",
    val content: String = "",
    val senderUid: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val attachments: List<MessageAttachment> = emptyList(),
    val read: Boolean = false,
    val syncStatus: String = SyncStatus.SYNCED,
    val lastSync: Timestamp = Timestamp.now()
)

@Parcelize
data class MessageAttachment(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val url: String = "",
    val size: Long = 0L,
    val fileExtension: String = ""
): Parcelable
