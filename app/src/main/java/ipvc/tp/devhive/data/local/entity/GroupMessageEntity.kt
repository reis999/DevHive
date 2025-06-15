package ipvc.tp.devhive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.local.converter.MessageAttachmentListConverter
import ipvc.tp.devhive.data.model.GroupMessage
import ipvc.tp.devhive.data.model.MessageAttachment
import ipvc.tp.devhive.data.util.SyncStatus

@Entity(tableName = "group_messages")
data class GroupMessageEntity(
    @PrimaryKey
    val id: String,
    val studyGroupId: String,
    val content: String,
    val senderUid: String,
    val senderName: String,
    val senderImageUrl: String,
    val createdAt: Timestamp,
    @field: TypeConverters(MessageAttachmentListConverter::class)
    val attachments: List<MessageAttachment>,
    val replyToMessageId: String? = null,
    val isEdited: Boolean = false,
    val editedAt: Timestamp? = null,
    val syncStatus: String,
    val lastSyncedAt: Long = 0L,
    val isLocalOnly: Boolean = false,
    val pendingOperation: String? = null
) {
    companion object {
        fun fromGroupMessage(groupMessage: GroupMessage, syncStatus: String = SyncStatus.SYNCED): GroupMessageEntity {
            return GroupMessageEntity(
                id = groupMessage.id,
                studyGroupId = groupMessage.studyGroupId,
                content = groupMessage.content,
                senderUid = groupMessage.senderUid,
                senderName = groupMessage.senderName,
                senderImageUrl = groupMessage.senderImageUrl,
                createdAt = groupMessage.createdAt,
                attachments = groupMessage.attachments,
                replyToMessageId = groupMessage.replyToMessageId,
                isEdited = groupMessage.isEdited,
                editedAt = groupMessage.editedAt,
                syncStatus = syncStatus,
                lastSyncedAt = System.currentTimeMillis(),
                isLocalOnly = syncStatus == SyncStatus.PENDING_UPLOAD,
                pendingOperation = if (syncStatus != SyncStatus.SYNCED) "UPDATE" else null
            )
        }

        fun toGroupMessage(entity: GroupMessageEntity): GroupMessage {
            return GroupMessage(
                id = entity.id,
                studyGroupId = entity.studyGroupId,
                content = entity.content,
                senderUid = entity.senderUid,
                senderName = entity.senderName,
                senderImageUrl = entity.senderImageUrl,
                createdAt = entity.createdAt,
                attachments = entity.attachments,
                replyToMessageId = entity.replyToMessageId,
                isEdited = entity.isEdited,
                editedAt = entity.editedAt
            )
        }
    }
}
