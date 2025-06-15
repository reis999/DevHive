package ipvc.tp.devhive.data.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import ipvc.tp.devhive.data.local.dao.MaterialDao
import ipvc.tp.devhive.data.local.entity.MaterialEntity
import ipvc.tp.devhive.data.model.Material
import ipvc.tp.devhive.data.remote.service.MaterialService
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID.randomUUID
import ipvc.tp.devhive.domain.repository.MaterialRepository as DomainMaterialRepository

class MaterialRepository(
    private val materialDao: MaterialDao,
    private val materialService: MaterialService,
    private val storage: FirebaseStorage,
    private val appScope: CoroutineScope
) : DomainMaterialRepository {

    override fun getAllMaterials(): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        appScope.launch {
            refreshMaterials()
        }

        return materialDao.getAllMaterials().map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    private suspend fun refreshMaterials() {
        withContext(Dispatchers.IO) {
            val result = materialService.getAllMaterials()
            if (result.isSuccess) {
                val materials = result.getOrThrow()
                val entities = materials.map { MaterialEntity.fromMaterial(it) }
                materialDao.insertMaterials(entities)
            }
        }
    }

    override suspend fun getMaterialById(materialId: String): ipvc.tp.devhive.domain.model.Material? {
        return withContext(Dispatchers.IO) {
            val localMaterial = materialDao.getMaterialById(materialId)

            if (localMaterial != null) {
                MaterialEntity.toMaterial(localMaterial).toDomainMaterial()
            } else {
                val remoteMaterial = materialService.getMaterialById(materialId)

                if (remoteMaterial != null) {
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(remoteMaterial))
                    materialService.incrementMaterialViews(materialId)
                    remoteMaterial.toDomainMaterial()
                } else {
                    null
                }
            }
        }
    }

    override fun observeMaterialById(materialId: String): LiveData<ipvc.tp.devhive.domain.model.Material?> {
        appScope.launch {
            refreshMaterial(materialId)
        }

        return materialDao.observeMaterialById(materialId).map { entity ->
            entity?.let { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    private suspend fun refreshMaterial(materialId: String) {
        withContext(Dispatchers.IO) {
            val remoteMaterial = materialService.getMaterialById(materialId)
            if (remoteMaterial != null) {
                materialDao.insertMaterial(MaterialEntity.fromMaterial(remoteMaterial))
            }
        }
    }

    override fun getMaterialsByUser(userId: String): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        appScope.launch {
            refreshMaterialsByUser(userId)
        }

        return materialDao.getMaterialsByUser(userId).map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    private suspend fun refreshMaterialsByUser(userId: String) {
        withContext(Dispatchers.IO) {
            val result = materialService.getMaterialsByUser(userId)
            if (result.isSuccess) {
                val materials = result.getOrThrow()
                val entities = materials.map { MaterialEntity.fromMaterial(it) }
                materialDao.insertMaterials(entities)
            }
        }
    }

    override fun getMaterialsBySubject(subject: String): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        appScope.launch {
            refreshMaterialsBySubject(subject)
        }

        return materialDao.getMaterialsBySubject(subject).map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    override fun searchMaterials(query: String): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        appScope.launch {
            refreshSearchResults(query)
        }

        return materialDao.searchMaterials(query).map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    override fun searchMaterialsBySubject(subject: String): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        appScope.launch {
            refreshMaterialsBySubject(subject)
        }

        return materialDao.getMaterialsBySubject(subject).map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    override fun searchMaterialsWithSubject(query: String, subject: String?): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        appScope.launch {
            refreshSearchWithSubject(query, subject)
        }

        return materialDao.searchMaterialsWithSubject(query, subject).map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    private suspend fun refreshSearchWithSubject(query: String, subject: String?) {
        withContext(Dispatchers.IO) {
            try {
                // Se tem subject específico, busca por subject primeiro
                val result = if (subject != null) {
                    materialService.getMaterialsBySubject(subject)
                } else {
                    // Se não tem subject, faz pesquisa geral
                    if (query.isNotBlank()) {
                        materialService.searchMaterials(query)
                    } else {
                        materialService.getAllMaterials()
                    }
                }

                if (result.isSuccess) {
                    val materials = result.getOrThrow()
                    val entities = materials.map { MaterialEntity.fromMaterial(it) }
                    materialDao.insertMaterials(entities)
                }else{
                    Log.e("MaterialRepository", "Error refreshing search with subject", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e("MaterialRepository", "Error refreshing search with subject", e)
            }
        }
    }

    override suspend fun getDistinctSubjects(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val localSubjects = materialDao.getDistinctSubjects()

                val remoteResult = materialService.getDistinctSubjects()
                if (remoteResult.isSuccess) {
                    val remoteSubjects = remoteResult.getOrThrow()
                    (localSubjects + remoteSubjects).distinct().sorted()
                } else {
                    localSubjects.sorted()
                }
            } catch (e: Exception) {
                materialDao.getDistinctSubjects().sorted()
            }
        }
    }

    private suspend fun refreshSearchResults(query: String) {
        withContext(Dispatchers.IO) {
            val result = materialService.searchMaterials(query)
            if (result.isSuccess) {
                val materials = result.getOrThrow()
                val entities = materials.map { MaterialEntity.fromMaterial(it) }
                materialDao.insertMaterials(entities)
            }
        }
    }

    private suspend fun refreshMaterialsBySubject(subject: String) {
        withContext(Dispatchers.IO) {
            val result = materialService.getMaterialsBySubject(subject)
            if (result.isSuccess) {
                val materials = result.getOrThrow()
                val entities = materials.map { MaterialEntity.fromMaterial(it) }
                materialDao.insertMaterials(entities)
            }
        }
    }

    override suspend fun createMaterial(
        material: ipvc.tp.devhive.domain.model.Material,
        fileUri: Uri,
        thumbnailUri: Uri?
    ): Result<ipvc.tp.devhive.domain.model.Material> {
        return withContext(Dispatchers.IO) {
            try {
                val contentUrl: String
                var thumbnailUrl = ""

                // Upload do arquivo principal
                val fileUploadResult = uploadMaterialFile(material.id, fileUri)
                if (fileUploadResult.isSuccess) {
                    contentUrl = fileUploadResult.getOrNull()!!
                } else {
                    Log.e("MaterialRepo", "Failed to upload file for material ${material.id}",
                        fileUploadResult.exceptionOrNull())
                    return@withContext Result.failure(
                        fileUploadResult.exceptionOrNull() ?: Exception("Erro no upload do arquivo")
                    )
                }

                // Upload da thumbnail
                if (thumbnailUri != null) {
                    val thumbnailUploadResult = uploadMaterialThumbnail(material.id, thumbnailUri)
                    if (thumbnailUploadResult.isSuccess) {
                        thumbnailUrl = thumbnailUploadResult.getOrNull()!!
                    } else {
                        Log.e("MaterialRepo", "Failed to upload thumbnail for material ${material.id}",
                            thumbnailUploadResult.exceptionOrNull())
                        // Continua sem thumbnail (não é crítico)
                    }
                }

                val updatedMaterial = material.copy(
                    contentUrl = contentUrl,
                    thumbnailUrl = thumbnailUrl,
                    updatedAt = Timestamp.now()
                )

                val dataMaterial = updatedMaterial.toDataMaterial()
                val result = materialService.createMaterial(dataMaterial)

                if (result.isSuccess) {
                    val createdMaterial = result.getOrThrow()
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(createdMaterial, SyncStatus.SYNCED))
                    Result.success(createdMaterial.toDomainMaterial())
                } else {
                    val materialEntity = MaterialEntity.fromMaterial(dataMaterial, SyncStatus.PENDING_UPLOAD)
                    materialDao.insertMaterial(materialEntity)
                    Result.success(updatedMaterial)
                }

            } catch (e: Exception) {
                val materialEntity = MaterialEntity.fromMaterial(
                    material.toDataMaterial(),
                    SyncStatus.PENDING_UPLOAD
                )
                materialDao.insertMaterial(materialEntity)
                Result.failure(e)
            }
        }
    }

    private suspend fun uploadMaterialFile(materialId: String, fileUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "material_${randomUUID()}"
                val fileRef = storage.reference.child("materials/$materialId/$fileName")

                fileRef.putFile(fileUri).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()

                Result.success(downloadUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun uploadMaterialThumbnail(materialId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "thumbnail_${randomUUID()}.jpg"
                val imageRef = storage.reference.child("material_thumbnails/$materialId/$fileName")

                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                Result.success(downloadUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateMaterial(material: ipvc.tp.devhive.domain.model.Material): Result<ipvc.tp.devhive.domain.model.Material> {
        return withContext(Dispatchers.IO) {
            try {
                val dataMaterial = material.toDataMaterial()
                val result = materialService.updateMaterial(dataMaterial)

                if (result.isSuccess) {
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(dataMaterial))
                    Result.success(material)
                } else {
                    val materialEntity = MaterialEntity.fromMaterial(dataMaterial, SyncStatus.PENDING_UPDATE)
                    materialDao.insertMaterial(materialEntity)
                    Result.success(material)
                }
            } catch (e: Exception) {
                val materialEntity = MaterialEntity.fromMaterial(material.toDataMaterial(), SyncStatus.PENDING_UPDATE)
                materialDao.insertMaterial(materialEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteMaterial(materialId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = materialService.deleteMaterial(materialId)

                if (result.isSuccess) {
                    materialDao.deleteMaterialById(materialId)
                    Result.success(true)
                } else {
                    val material = materialDao.getMaterialById(materialId)
                    if (material != null) {
                        val updatedMaterial = material.copy(syncStatus = SyncStatus.PENDING_DELETE)
                        materialDao.updateMaterial(updatedMaterial)
                    }
                    Result.success(false)
                }
            } catch (e: Exception) {
                val material = materialDao.getMaterialById(materialId)
                if (material != null) {
                    val updatedMaterial = material.copy(syncStatus = SyncStatus.PENDING_DELETE)
                    materialDao.updateMaterial(updatedMaterial)
                }
                Result.failure(e)
            }
        }
    }

    override suspend fun incrementDownloads(materialId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val material = materialDao.getMaterialById(materialId)
                if (material != null) {
                    val updatedMaterial = material.copy(downloads = material.downloads + 1)
                    materialDao.updateMaterial(updatedMaterial)
                    materialService.incrementMaterialDownloads(materialId)

                    Result.success(true)
                } else {
                    Result.failure(Exception("Material not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun incrementViews(materialId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val material = materialDao.getMaterialById(materialId)
                if (material != null) {
                    val updatedMaterial = material.copy(views = material.views + 1)
                    materialDao.updateMaterial(updatedMaterial)

                    materialService.incrementMaterialViews(materialId)

                    Result.success(true)
                } else {
                    Result.failure(Exception("Material not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getBookmarkedMaterials(userId: String): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        return materialDao.getAllMaterials().map { entities ->
            entities
                .map { MaterialEntity.toMaterial(it).toDomainMaterial() }
                .filter { material -> userId in material.bookmarkedBy }
        }
    }

    override suspend fun toggleBookmark(materialId: String, userId: String, isBookmarked: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = materialService.toggleMaterialBookmark(materialId, userId, isBookmarked)

                if (result.isSuccess) {
                    val material = materialDao.getMaterialById(materialId)
                    if (material != null) {
                        val currentBookmarkedBy = material.bookmarkedBy.toMutableList()
                        val newBookmarkedBy = if (isBookmarked) {
                            if (userId !in currentBookmarkedBy) currentBookmarkedBy + userId else currentBookmarkedBy
                        } else {
                            currentBookmarkedBy - userId
                        }

                        val updatedMaterial = material.copy(
                            bookmarks = newBookmarkedBy.size,
                            bookmarkedBy = newBookmarkedBy
                        )
                        materialDao.updateMaterial(updatedMaterial)
                    }
                    Result.success(true)
                } else {
                    Result.failure(Exception("Erro ao sincronizar bookmark com servidor"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun toggleMaterialLike(materialId: String, userId: String, isLiked: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = materialService.toggleMaterialLike(materialId, userId, isLiked)

                if (result.isSuccess) {
                    val material = materialDao.getMaterialById(materialId)
                    if (material != null) {
                        val currentLikedBy = material.likedBy.toMutableList()
                        val newLikedBy = if (isLiked) {
                            if (userId !in currentLikedBy) currentLikedBy + userId else currentLikedBy
                        } else {
                            currentLikedBy - userId
                        }

                        val updatedMaterial = material.copy(
                            likes = newLikedBy.size,
                            likedBy = newLikedBy
                        )
                        materialDao.updateMaterial(updatedMaterial)
                    }
                    Result.success(true)
                } else {
                    Result.failure(Exception("Erro ao sincronizar like com servidor"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun syncPendingMaterials() {
        withContext(Dispatchers.IO) {
            val unsyncedMaterials = materialDao.getUnsyncedMaterials()

            for (materialEntity in unsyncedMaterials) {
                when (materialEntity.syncStatus) {
                    SyncStatus.PENDING_UPLOAD -> {
                        val material = MaterialEntity.toMaterial(materialEntity)
                        val result = materialService.createMaterial(material)
                        if (result.isSuccess) {
                            materialDao.updateSyncStatus(materialEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_UPDATE -> {
                        val material = MaterialEntity.toMaterial(materialEntity)
                        val result = materialService.updateMaterial(material)
                        if (result.isSuccess) {
                            materialDao.updateSyncStatus(materialEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_DELETE -> {
                        val result = materialService.deleteMaterial(materialEntity.id)
                        if (result.isSuccess) {
                            materialDao.deleteMaterialById(materialEntity.id)
                        }
                    }
                }
            }
        }
    }

    private fun Material.toDomainMaterial(): ipvc.tp.devhive.domain.model.Material {
        return ipvc.tp.devhive.domain.model.Material(
            id = this.id,
            title = this.title,
            description = this.description,
            subject = this.subject,
            type = this.type,
            contentUrl = this.contentUrl,
            fileSize = this.fileSize,
            thumbnailUrl = this.thumbnailUrl,
            ownerUid = this.ownerUid,
            ownerName = this.ownerName,
            ownerImageUrl = this.ownerImageUrl,
            views = this.views,
            downloads = this.downloads,
            rating = this.rating,
            categories = this.categories,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            likes = this.likes,
            bookmarkedBy = this.bookmarkedBy,
            bookmarks = this.bookmarks,
            likedBy = this.likedBy,
            isPublic = this.isPublic,
            reviewCount = this.reviewCount
        )
    }

    private fun ipvc.tp.devhive.domain.model.Material.toDataMaterial(): Material {
        return Material(
            id = this.id,
            title = this.title,
            description = this.description,
            subject = this.subject,
            type = this.type,
            contentUrl = this.contentUrl,
            fileSize = this.fileSize,
            thumbnailUrl = this.thumbnailUrl,
            ownerUid = this.ownerUid,
            ownerName = this.ownerName,
            ownerImageUrl = this.ownerImageUrl,
            views = this.views,
            downloads = this.downloads,
            rating = this.rating,
            categories = this.categories,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            likes = this.likes,
            bookmarkedBy = this.bookmarkedBy,
            bookmarks = this.bookmarks,
            likedBy = this.likedBy,
            isPublic = this.isPublic,
            reviewCount = this.reviewCount
        )
    }
}
