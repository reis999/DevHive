package ipvc.tp.devhive.domain.usecase.material

import ipvc.tp.devhive.domain.repository.MaterialRepository

/**
 * Caso de uso para marcar/desmarcar um material como favorito
 */
class ToggleBookmarkUseCase(private val materialRepository: MaterialRepository) {

    suspend operator fun invoke(materialId: String, bookmarked: Boolean): Result<Boolean> {
        return materialRepository.toggleBookmark(materialId, bookmarked)
    }
}
