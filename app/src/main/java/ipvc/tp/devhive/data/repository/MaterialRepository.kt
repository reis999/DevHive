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

class MaterialRepository(
    private val materialDao: MaterialDao,
    private val materialService: MaterialService,
    private val appScope: CoroutineScope
) {
    fun getAllMaterials(): LiveData<List<Material>> {
        appScope.launch {
            refreshMaterials()
        }

        return materialDao.getAllMaterials().map { entities ->
            entities.map { MaterialEntity.toMaterial(it) }
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

    suspend fun getMaterialById(materialId: String): Material? {
        return withContext(Dispatchers.IO) {
            val localMaterial = materialDao.getMaterialById(materialId)

            if (localMaterial != null) {
                MaterialEntity.toMaterial(localMaterial)
            } else {
                val remoteMaterial = materialService.getMaterialById(materialId)

                if (remoteMaterial != null) {
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(remoteMaterial))

                    materialService.incrementMaterialViews(materialId)
                }

                remoteMaterial
            }
        }
    }

    fun observeMaterialById(materialId: String): LiveData<Material?> {
        appScope.launch {
            refreshMaterial(materialId)
        }

        return materialDao.observeMaterialById(materialId).map { entity ->
            entity?.let { MaterialEntity.toMaterial(it) }
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

    fun getMaterialsByUser(userId: String): LiveData<List<Material>> {
        appScope.launch {
            refreshMaterialsByUser(userId)
        }

        return materialDao.getMaterialsByUser(userId).map { entities ->
            entities.map { MaterialEntity.toMaterial(it) }
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

    fun getMaterialsBySubject(subject: String): LiveData<List<Material>> {
        appScope.launch {
            refreshMaterialsBySubject(subject)
        }

        return materialDao.getMaterialsBySubject(subject).map { entities ->
            entities.map { MaterialEntity.toMaterial(it) }
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

    suspend fun createMaterial(material: Material): Result<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val result = materialService.createMaterial(material)

                if (result.isSuccess) {
                    val createdMaterial = result.getOrThrow()
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(createdMaterial))
                    Result.success(createdMaterial)
                } else {
                    val materialEntity = MaterialEntity.fromMaterial(material, SyncStatus.PENDING_UPLOAD)
                    materialDao.insertMaterial(materialEntity)
                    Result.success(material)
                }
            } catch (e: Exception) {
                val materialEntity = MaterialEntity.fromMaterial(material, SyncStatus.PENDING_UPLOAD)
                materialDao.insertMaterial(materialEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun updateMaterial(material: Material): Result<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val result = materialService.updateMaterial(material)

                if (result.isSuccess) {
                    materialDao.insertMaterial(MaterialEntity.fromMaterial(material))
                    Result.success(material)
                } else {
                    val materialEntity = MaterialEntity.fromMaterial(material, SyncStatus.PENDING_UPDATE)
                    materialDao.insertMaterial(materialEntity)
                    Result.success(material)
                }
            } catch (e: Exception) {
                val materialEntity = MaterialEntity.fromMaterial(material, SyncStatus.PENDING_UPDATE)
                materialDao.insertMaterial(materialEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteMaterial(materialId: String): Result<Boolean> {
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

    fun getBookmarkedMaterials(): LiveData<List<Material>> {
        return materialDao.getBookmarkedMaterials().map { entities ->
            entities.map { MaterialEntity.toMaterial(it) }
        }
    }

    suspend fun toggleBookmark(materialId: String, bookmarked: Boolean): Result<Boolean> {
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

    suspend fun syncPendingMaterials() {
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
}
