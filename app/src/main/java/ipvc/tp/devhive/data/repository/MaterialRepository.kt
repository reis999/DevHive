package ipvc.tp.devhive.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ipvc.tp.devhive.data.local.dao.MaterialDao
import ipvc.tp.devhive.data.local.entity.MaterialEntity
import ipvc.tp.devhive.data.model.Material
import ipvc.tp.devhive.data.remote.service.MaterialService
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ipvc.tp.devhive.domain.repository.MaterialRepository as DomainMaterialRepository

class MaterialRepository(
    private val materialDao: MaterialDao,
    private val materialService: MaterialService,
    private val appScope: CoroutineScope
) : DomainMaterialRepository {

    override fun getAllMaterials(): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshMaterials()
        }

        // Retorna LiveData do banco local
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
            // Primeiro tenta obter do banco de dados local
            val localMaterial = materialDao.getMaterialById(materialId)

            if (localMaterial != null) {
                MaterialEntity.toMaterial(localMaterial).toDomainMaterial()
            } else {
                // Se não encontrar localmente, busca do Firestore
                val remoteMaterial = materialService.getMaterialById(materialId)

                // Se encontrar remotamente, salva no banco local
                if (remoteMaterial != null) {
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(remoteMaterial))

                    // Incrementa visualizações
                    materialService.incrementMaterialViews(materialId)
                    remoteMaterial.toDomainMaterial()
                } else {
                    null
                }
            }
        }
    }

    override fun observeMaterialById(materialId: String): LiveData<ipvc.tp.devhive.domain.model.Material?> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshMaterial(materialId)
        }

        // Retorna LiveData do banco local
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
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshMaterialsByUser(userId)
        }

        // Retorna LiveData do banco local
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
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshMaterialsBySubject(subject)
        }

        // Retorna LiveData do banco local
        return materialDao.getMaterialsBySubject(subject).map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
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

    override suspend fun createMaterial(material: ipvc.tp.devhive.domain.model.Material): Result<ipvc.tp.devhive.domain.model.Material> {
        return withContext(Dispatchers.IO) {
            try {
                val dataMaterial = material.toDataMaterial()
                // Tenta criar no Firestore
                val result = materialService.createMaterial(dataMaterial)

                if (result.isSuccess) {
                    // Se sucesso, salva no banco local
                    val createdMaterial = result.getOrThrow()
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(createdMaterial))
                    Result.success(createdMaterial.toDomainMaterial())
                } else {
                    // Se falhar, salva localmente com status pendente
                    val materialEntity = MaterialEntity.fromMaterial(dataMaterial, SyncStatus.PENDING_UPLOAD)
                    materialDao.insertMaterial(materialEntity)
                    Result.success(material)
                }
            } catch (e: Exception) {
                // Em caso de erro, salva localmente com status pendente
                val materialEntity = MaterialEntity.fromMaterial(material.toDataMaterial(), SyncStatus.PENDING_UPLOAD)
                materialDao.insertMaterial(materialEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun updateMaterial(material: ipvc.tp.devhive.domain.model.Material): Result<ipvc.tp.devhive.domain.model.Material> {
        return withContext(Dispatchers.IO) {
            try {
                val dataMaterial = material.toDataMaterial()
                // Tenta atualizar no Firestore
                val result = materialService.updateMaterial(dataMaterial)

                if (result.isSuccess) {
                    // Se sucesso, atualiza no banco local
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(dataMaterial))
                    Result.success(material)
                } else {
                    // Se falhar, atualiza localmente com status pendente
                    val materialEntity = MaterialEntity.fromMaterial(dataMaterial, SyncStatus.PENDING_UPDATE)
                    materialDao.insertMaterial(materialEntity)
                    Result.success(material)
                }
            } catch (e: Exception) {
                // Em caso de erro, atualiza localmente com status pendente
                val materialEntity = MaterialEntity.fromMaterial(material.toDataMaterial(), SyncStatus.PENDING_UPDATE)
                materialDao.insertMaterial(materialEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteMaterial(materialId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Tenta deletar no Firestore
                val result = materialService.deleteMaterial(materialId)

                if (result.isSuccess) {
                    // Se sucesso, remove do banco local
                    materialDao.deleteMaterialById(materialId)
                    Result.success(true)
                } else {
                    // Se falhar, marca como pendente de deleção
                    val material = materialDao.getMaterialById(materialId)
                    if (material != null) {
                        val updatedMaterial = material.copy(syncStatus = SyncStatus.PENDING_DELETE)
                        materialDao.updateMaterial(updatedMaterial)
                    }
                    Result.success(false)
                }
            } catch (e: Exception) {
                // Em caso de erro, marca como pendente de deleção
                val material = materialDao.getMaterialById(materialId)
                if (material != null) {
                    val updatedMaterial = material.copy(syncStatus = SyncStatus.PENDING_DELETE)
                    materialDao.updateMaterial(updatedMaterial)
                }
                Result.failure(e)
            }
        }
    }

    override fun getBookmarkedMaterials(): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        return materialDao.getBookmarkedMaterials().map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    override suspend fun getUserBookmarks(userId: String): LiveData<List<ipvc.tp.devhive.domain.model.Material>> {
        return materialDao.getUserBookmarks(userId).map { entities ->
            entities.map { MaterialEntity.toMaterial(it).toDomainMaterial() }
        }
    }

    override suspend fun toggleBookmark(materialId: String, bookmarked: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val material = materialDao.getMaterialById(materialId)
                if (material != null) {
                    val updatedMaterial = material.copy(bookmarked = bookmarked)
                    materialDao.updateMaterial(updatedMaterial)
                    Result.success(true)
                } else {
                    Result.failure(Exception("Material not found"))
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

    // Funções de extensão para converter entre modelos data e domain
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
            bookmarked = this.bookmarked,
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
            bookmarked = this.bookmarked,
            isPublic = this.isPublic,
            reviewCount = this.reviewCount
        )
    }
}
