package ipvc.tp.devhive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ipvc.tp.devhive.data.local.converter.StringListConverter
import ipvc.tp.devhive.data.model.StudyGroup
import ipvc.tp.devhive.data.util.SyncStatus
import com.google.firebase.Timestamp

@Entity(tableName = "study_groups")
@TypeConverters(StringListConverter::class)
data class StudyGroupEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val imageUrl: String,
    val members: List<String>,
    val admins: List<String>,
    val categories: List<String>,
    val isPrivate: Boolean,
    val joinCode: String,
    val maxMembers: Int,
    val lastMessageAt: Long?,
    val lastMessagePreview: String,
    val messageCount: Int,

    // Campos para sincronização
    val syncStatus: String,
    val lastSyncedAt: Long,
    val isLocalOnly: Boolean,
    val pendingOperation: String?
) {
    companion object {
        fun fromStudyGroup(studyGroup: StudyGroup, syncStatus: String = SyncStatus.SYNCED): StudyGroupEntity {
            return StudyGroupEntity(
                id = studyGroup.id,
                name = studyGroup.name,
                description = studyGroup.description,
                createdBy = studyGroup.createdBy,
                createdAt = studyGroup.createdAt.seconds * 1000 + studyGroup.createdAt.nanoseconds / 1000000,
                updatedAt = studyGroup.updatedAt.seconds * 1000 + studyGroup.updatedAt.nanoseconds / 1000000,
                imageUrl = studyGroup.imageUrl,
                members = studyGroup.members,
                admins = studyGroup.admins,
                categories = studyGroup.categories,
                isPrivate = studyGroup.isPrivate,
                joinCode = studyGroup.joinCode,
                maxMembers = studyGroup.maxMembers,
                lastMessageAt = studyGroup.lastMessageAt?.let { it.seconds * 1000 + it.nanoseconds / 1000000 },
                lastMessagePreview = studyGroup.lastMessagePreview,
                messageCount = studyGroup.messageCount,
                syncStatus = syncStatus,
                lastSyncedAt = System.currentTimeMillis(),
                isLocalOnly = syncStatus == SyncStatus.PENDING_UPLOAD,
                pendingOperation = if (syncStatus != SyncStatus.SYNCED) "UPDATE" else null
            )
        }

        fun toStudyGroup(entity: StudyGroupEntity): StudyGroup {
            return StudyGroup(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                createdBy = entity.createdBy,
                createdAt = Timestamp(entity.createdAt / 1000, ((entity.createdAt % 1000) * 1000000).toInt()),
                updatedAt = Timestamp(entity.updatedAt / 1000, ((entity.updatedAt % 1000) * 1000000).toInt()),
                imageUrl = entity.imageUrl,
                members = entity.members,
                admins = entity.admins,
                categories = entity.categories,
                isPrivate = entity.isPrivate,
                joinCode = entity.joinCode,
                maxMembers = entity.maxMembers,
                lastMessageAt = entity.lastMessageAt?.let { Timestamp(it / 1000, ((it % 1000) * 1000000).toInt()) },
                lastMessagePreview = entity.lastMessagePreview,
                messageCount = entity.messageCount
            )
        }
    }
}
