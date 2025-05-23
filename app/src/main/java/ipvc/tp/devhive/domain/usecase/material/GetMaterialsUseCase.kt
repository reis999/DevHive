package ipvc.tp.devhive.domain.usecase.material

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.repository.MaterialRepository

/**
 * Caso de uso para obter materiais de estudo
 */
class GetMaterialsUseCase(private val materialRepository: MaterialRepository) {

    operator fun invoke(): LiveData<List<Material>> {
        return materialRepository.getAllMaterials()
    }

    fun byUser(userId: String): LiveData<List<Material>> {
        return materialRepository.getMaterialsByUser(userId)
    }

    fun bySubject(subject: String): LiveData<List<Material>> {
        return materialRepository.getMaterialsBySubject(subject)
    }

    fun bookmarked(): LiveData<List<Material>> {
        return materialRepository.getBookmarkedMaterials()
    }
}
