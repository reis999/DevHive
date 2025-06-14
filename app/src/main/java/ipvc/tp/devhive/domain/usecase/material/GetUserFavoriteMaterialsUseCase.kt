package ipvc.tp.devhive.domain.usecase.material

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.repository.MaterialRepository
import javax.inject.Inject

class GetUserFavoriteMaterialsUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
)
{
    suspend operator fun invoke(userId: String): LiveData<List<Material>> {
        return materialRepository.getUserBookmarks(userId)
    }
}