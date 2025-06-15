package ipvc.tp.devhive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import ipvc.tp.devhive.data.model.ContributionStats
import ipvc.tp.devhive.data.model.User
import ipvc.tp.devhive.data.util.SyncStatus


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val profileImageUrl: String,
    val bio: String,
    val institution: String,
    val course: String,
    val createdAt: Long,
    val lastLogin: Long,
    @get:PropertyName("online") val isOnline: Boolean,
    val materialCount: Int,
    val commentCount: Int,
    val likeCount: Int,
    val sessionCount: Int,
    val syncStatus: String,
    val lastSyncedAt: Long,
    val isLocalOnly: Boolean,
    val pendingOperation: String?
) {
    companion object {
        fun fromUser(user: User, syncStatus: String = SyncStatus.SYNCED): UserEntity {
            return UserEntity(
                id = user.id,
                name = user.name,
                username = user.username,
                email = user.email,
                profileImageUrl = user.profileImageUrl,
                bio = user.bio,
                institution = user.institution,
                course = user.course,
                createdAt = user.createdAt.seconds * 1000 + user.createdAt.nanoseconds / 1000000,
                lastLogin = user.lastLogin.seconds * 1000 + user.lastLogin.nanoseconds / 1000000,
                isOnline = user.online,
                materialCount = user.contributionStats.materials,
                commentCount = user.contributionStats.comments,
                likeCount = user.contributionStats.likes,
                sessionCount = user.contributionStats.sessions,
                syncStatus = syncStatus,
                lastSyncedAt = System.currentTimeMillis(),
                isLocalOnly = syncStatus == SyncStatus.PENDING_UPLOAD,
                pendingOperation = if (syncStatus != SyncStatus.SYNCED) "UPDATE" else null
            )
        }

        fun toUser(entity: UserEntity): User {
            return User(
                id = entity.id,
                name = entity.name,
                username = entity.username,
                email = entity.email,
                profileImageUrl = entity.profileImageUrl,
                bio = entity.bio,
                institution = entity.institution,
                course = entity.course,
                createdAt = Timestamp(entity.createdAt / 1000, ((entity.createdAt % 1000) * 1000000).toInt()),
                lastLogin = Timestamp(entity.lastLogin / 1000, ((entity.lastLogin % 1000) * 1000000).toInt()),
                online = entity.isOnline,
                contributionStats = ContributionStats(
                    materials = entity.materialCount,
                    comments = entity.commentCount,
                    likes = entity.likeCount,
                    sessions = entity.sessionCount
                )
            )
        }
    }
}