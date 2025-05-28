package ipvc.tp.devhive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.local.converter.MessageAttachmentListConverter
import ipvc.tp.devhive.data.model.Message
import ipvc.tp.devhive.data.model.MessageAttachment
import ipvc.tp.devhive.data.util.SyncStatus

@Entity(tableName = "messages")
@TypeConverters(MessageAttachmentListConverter::class)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val content: String,
    val senderUid: String,
    val createdAt: Long,
    val attachments: List<MessageAttachment>,
    val read: Boolean,
    val syncStatus: String,
    val lastSyncTimestamp: Long,

    // Campos para sincronização
    val lastSyncedAt: Long,
    val isLocalOnly: Boolean,
    val pendingOperation: String?
) {
    companion object {
        fun fromMessage(message: Message, syncStatus: String = SyncStatus.SYNCED): MessageEntity {
            return MessageEntity(
                id = message.id,
                chatId = message.chatId,
                content = message.content,
                senderUid = message.senderUid,
                createdAt = message.createdAt.seconds * 1000 + message.createdAt.nanoseconds / 1000000,
                attachments = message.attachments,
                read = message.read,
                syncStatus = syncStatus,
                lastSyncTimestamp = message.lastSync.seconds * 1000 + message.lastSync.nanoseconds / 1000000,
                lastSyncedAt = System.currentTimeMillis(),
                isLocalOnly = syncStatus == SyncStatus.PENDING_UPLOAD,
                pendingOperation = if (syncStatus != SyncStatus.SYNCED) "UPDATE" else null
            )
        }

        fun toMessage(entity: MessageEntity): Message {
            return Message(
                id = entity.id,
                chatId = entity.chatId,
                content = entity.content,
                senderUid = entity.senderUid,
                createdAt = Timestamp(entity.createdAt / 1000, ((entity.createdAt % 1000) * 1000000).toInt()),
                attachments = entity.attachments,
                read = entity.read,
                syncStatus = entity.syncStatus,
                lastSync = Timestamp(entity.lastSyncTimestamp / 1000, ((entity.lastSyncTimestamp % 1000) * 1000000).toInt())
            )
        }
    }
}
