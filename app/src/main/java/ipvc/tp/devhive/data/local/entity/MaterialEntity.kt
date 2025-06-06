package ipvc.tp.devhive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.local.converter.StringListConverter
import ipvc.tp.devhive.data.model.Material
import ipvc.tp.devhive.data.util.SyncStatus

@Entity(tableName = "materials")
@TypeConverters(StringListConverter::class)
data class MaterialEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val contentUrl: String,
    val thumbnailUrl: String,
    val fileSize: Long,
    val ownerUid: String,
    val ownerName: String,
    val ownerImageUrl: String,
    val createdAt: Long,
    val updatedAt: Long,
    val categories: List<String>,
    val isPublic: Boolean,
    val subject: String,
    val views: Int,
    val likes: Int,
    val downloads: Int,
    val bookmarked: Boolean,
    val rating: Float,
    val reviewCount: Int,
    val lastSyncTimestamp: Long,

    // Campos para sincronização
    val syncStatus: String,
    val lastSyncedAt: Long,
    val isLocalOnly: Boolean,
    val pendingOperation: String?
) {
    companion object {
        fun fromMaterial(material: Material, syncStatus: String = SyncStatus.SYNCED): MaterialEntity {
            return MaterialEntity(
                id = material.id,
                title = material.title,
                description = material.description,
                type = material.type,
                contentUrl = material.contentUrl,
                thumbnailUrl = material.thumbnailUrl,
                fileSize = material.fileSize,
                ownerUid = material.ownerUid,
                ownerName = material.ownerName,
                ownerImageUrl = material.ownerImageUrl,
                createdAt = material.createdAt.seconds * 1000 + material.createdAt.nanoseconds / 1000000,
                updatedAt = material.updatedAt.seconds * 1000 + material.updatedAt.nanoseconds / 1000000,
                categories = material.categories,
                isPublic = material.isPublic,
                subject = material.subject,
                views = material.views,
                likes = material.likes,
                downloads = material.downloads,
                bookmarked = material.bookmarked,
                rating = material.rating,
                reviewCount = material.reviewCount,
                lastSyncTimestamp = material.lastSync.seconds * 1000 + material.lastSync.nanoseconds / 1000000,
                syncStatus = syncStatus,
                lastSyncedAt = System.currentTimeMillis(),
                isLocalOnly = syncStatus == SyncStatus.PENDING_UPLOAD,
                pendingOperation = if (syncStatus != SyncStatus.SYNCED) "UPDATE" else null
            )
        }

        fun toMaterial(entity: MaterialEntity): Material {
            return Material(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                type = entity.type,
                contentUrl = entity.contentUrl,
                thumbnailUrl = entity.thumbnailUrl,
                fileSize = entity.fileSize,
                ownerUid = entity.ownerUid,
                ownerName = entity.ownerName,
                ownerImageUrl = entity.ownerImageUrl,
                createdAt = Timestamp(entity.createdAt / 1000, ((entity.createdAt % 1000) * 1000000).toInt()),
                updatedAt = Timestamp(entity.updatedAt / 1000, ((entity.updatedAt % 1000) * 1000000).toInt()),
                categories = entity.categories,
                isPublic = entity.isPublic,
                subject = entity.subject,
                views = entity.views,
                likes = entity.likes,
                downloads = entity.downloads,
                bookmarked = entity.bookmarked,
                rating = entity.rating,
                reviewCount = entity.reviewCount,
                lastSync = Timestamp(entity.lastSyncTimestamp / 1000, ((entity.lastSyncTimestamp % 1000) * 1000000).toInt())
            )
        }
    }
}
