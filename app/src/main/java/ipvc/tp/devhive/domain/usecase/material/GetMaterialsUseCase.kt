package ipvc.tp.devhive.domain.usecase.material

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.repository.MaterialRepository
import javax.inject.Inject

class GetMaterialsUseCase @Inject constructor(private val materialRepository: MaterialRepository) {

    operator fun invoke(): LiveData<List<Material>> {
        return materialRepository.getAllMaterials()
    }

    fun byUser(userId: String): LiveData<List<Material>> {
        return materialRepository.getMaterialsByUser(userId)
    }

    fun bySubject(subject: String): LiveData<List<Material>> {
        return materialRepository.getMaterialsBySubject(subject)
    }

    fun bookmarked(userId: String): LiveData<List<Material>> {
        return materialRepository.getBookmarkedMaterials(userId)
    }

    suspend fun byId(materialId: String): Material? {
        return materialRepository.getMaterialById(materialId)
    }

    fun search(query: String): LiveData<List<Material>> {
        return materialRepository.searchMaterials(query)
    }

    fun searchBySubject(subject: String): LiveData<List<Material>> {
        return materialRepository.searchMaterialsBySubject(subject)
    }

    suspend fun getDistinctSubjects(): List<String> {
        return materialRepository.getDistinctSubjects()
    }

    fun searchWithSubject(query: String, subject: String?): LiveData<List<Material>> {
        return materialRepository.searchMaterialsWithSubject(query, subject)
    }
}
