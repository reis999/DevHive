package ipvc.tp.devhive.domain.repository

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Material

/**
 * Interface de repositório para operações relacionadas a materiais de estudo
 */
interface MaterialRepository {
    fun getAllMaterials(): LiveData<List<Material>>
    suspend fun getMaterialById(materialId: String): Material?
    fun observeMaterialById(materialId: String): LiveData<Material?>
    fun getMaterialsByUser(userId: String): LiveData<List<Material>>
    fun getMaterialsBySubject(subject: String): LiveData<List<Material>>
    suspend fun createMaterial(material: Material): Result<Material>
    suspend fun updateMaterial(material: Material): Result<Material>
    suspend fun deleteMaterial(materialId: String): Result<Boolean>
    fun getBookmarkedMaterials(): LiveData<List<Material>>
    suspend fun getUserBookmarks(userId: String): LiveData<List<Material>>
    suspend fun toggleBookmark(materialId: String, bookmarked: Boolean): Result<Boolean>
    suspend fun syncPendingMaterials()
}
