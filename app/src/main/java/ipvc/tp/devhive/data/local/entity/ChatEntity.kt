package ipvc.tp.devhive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.model.Chat
import ipvc.tp.devhive.data.util.SyncStatus

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val participant1Id: String,
    val participant2Id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessageAt: Long,
    val lastMessagePreview: String,
    val messageCount: Int,

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
                participant1Id = chat.participant1Id,
                participant2Id = chat.participant2Id,
                createdAt = chat.createdAt.seconds * 1000 + chat.createdAt.nanoseconds / 1000000,
                updatedAt = chat.updatedAt.seconds * 1000 + chat.updatedAt.nanoseconds / 1000000,
                lastMessageAt = chat.lastMessageAt.seconds * 1000 + chat.lastMessageAt.nanoseconds / 1000000,
                lastMessagePreview = chat.lastMessagePreview,
                messageCount = chat.messageCount,
                syncStatus = syncStatus,
                lastSyncedAt = System.currentTimeMillis(),
                isLocalOnly = syncStatus == SyncStatus.PENDING_UPLOAD,
                pendingOperation = if (syncStatus != SyncStatus.SYNCED) "UPDATE" else null
            )
        }

        fun toChat(entity: ChatEntity): Chat {
            return Chat(
                id = entity.id,
                participant1Id = entity.participant1Id,
                participant2Id = entity.participant2Id,
                createdAt = Timestamp(entity.createdAt / 1000, ((entity.createdAt % 1000) * 1000000).toInt()),
                updatedAt = Timestamp(entity.updatedAt / 1000, ((entity.updatedAt % 1000) * 1000000).toInt()),
                lastMessageAt = Timestamp(entity.lastMessageAt / 1000, ((entity.lastMessageAt % 1000) * 1000000).toInt()),
                lastMessagePreview = entity.lastMessagePreview,
                messageCount = entity.messageCount
            )
        }
    }
}

