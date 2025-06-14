package ipvc.tp.devhive.domain.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Material
import kotlinx.coroutines.flow.Flow

/**
 * Interface de repositório para operações relacionadas a materiais de estudo
 */
interface MaterialRepository {
    fun getAllMaterials(): LiveData<List<Material>>
    suspend fun getMaterialById(materialId: String): Material?
    fun observeMaterialById(materialId: String): LiveData<Material?>
    fun getMaterialsByUser(userId: String): LiveData<List<Material>>
    fun getMaterialsBySubject(subject: String): LiveData<List<Material>>
    suspend fun createMaterial(material: Material, fileUri: Uri, thumbnailUri: Uri?): Result<Material>
    suspend fun updateMaterial(material: Material): Result<Material>
    suspend fun deleteMaterial(materialId: String): Result<Boolean>
    fun getBookmarkedMaterials(userId: String): LiveData<List<Material>>
    suspend fun toggleBookmark(materialId: String, userId: String, isBookmarked: Boolean): Result<Boolean>
    suspend fun incrementDownloads(materialId: String): Result<Boolean>
    suspend fun toggleMaterialLike(materialId: String, userId: String, isLiked: Boolean): Result<Boolean>
    fun searchMaterials(query: String): LiveData<List<Material>>
    fun searchMaterialsBySubject(subject: String): LiveData<List<Material>>
    suspend fun getDistinctSubjects(): List<String>
    fun searchMaterialsWithSubject(query: String, subject: String?): LiveData<List<Material>>
    suspend fun syncPendingMaterials()
}
