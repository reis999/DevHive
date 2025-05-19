package ipvc.tp.devhive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.local.converter.StringListConverter
import ipvc.tp.devhive.data.model.Chat
import ipvc.tp.devhive.data.util.SyncStatus

@Entity(tableName = "chats")
@TypeConverters(StringListConverter::class)
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val isPrivate: Boolean,
    val accessCode: String,
    val maxCapacity: Int,
    val participantIds: List<String>,
    val creatorUid: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessageAt: Long,
    val lastMessagePreview: String,
    val messageCount: Int,
    val lastSyncTimestamp: Long,

    // Campos para sincronização
    val syncStatus: String,
    val lastSyncedAt: Long,
    val isLocalOnly: Boolean,
    val pendingOperation: String?
) {
    companion object {
        fun fromChat(chat: Chat, syncStatus: String = SyncStatus.SYNCED): ChatEntity {
            return ChatEntity(
                id = chat.id,
                name = chat.name,
                isPrivate = chat.isPrivate,
                accessCode = chat.accessCode,
                maxCapacity = chat.maxCapacity,
                participantIds = chat.participantIds,
                creatorUid = chat.creatorUid,
                createdAt = chat.createdAt.seconds * 1000 + chat.createdAt.nanoseconds / 1000000,
                updatedAt = chat.updatedAt.seconds * 1000 + chat.updatedAt.nanoseconds / 1000000,
                lastMessageAt = chat.lastMessageAt.seconds * 1000 + chat.lastMessageAt.nanoseconds / 1000000,
                lastMessagePreview = chat.lastMessagePreview,
                messageCount = chat.messageCount,
                lastSyncTimestamp = chat.lastSync.seconds * 1000 + chat.lastSync.nanoseconds / 1000000,
                syncStatus = syncStatus,
                lastSyncedAt = System.currentTimeMillis(),
                isLocalOnly = syncStatus == SyncStatus.PENDING_UPLOAD,
                pendingOperation = if (syncStatus != SyncStatus.SYNCED) "UPDATE" else null
            )
        }

        fun toChat(entity: ChatEntity): Chat {
            return Chat(
                id = entity.id,
                name = entity.name,
                isPrivate = entity.isPrivate,
                accessCode = entity.accessCode,
                maxCapacity = entity.maxCapacity,
                participantIds = entity.participantIds,
                creatorUid = entity.creatorUid,
                createdAt = Timestamp(entity.createdAt / 1000, ((entity.createdAt % 1000) * 1000000).toInt()),
                updatedAt = Timestamp(entity.updatedAt / 1000, ((entity.updatedAt % 1000) * 1000000).toInt()),
                lastMessageAt = Timestamp(entity.lastMessageAt / 1000, ((entity.lastMessageAt % 1000) * 1000000).toInt()),
                lastMessagePreview = entity.lastMessagePreview,
                messageCount = entity.messageCount,
                lastSync = Timestamp(entity.lastSyncTimestamp / 1000, ((entity.lastSyncTimestamp % 1000) * 1000000).toInt())
            )
        }
    }
}
