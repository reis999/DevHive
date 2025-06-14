package ipvc.tp.devhive.domain.usecase.material

import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para marcar/desmarcar um material como favorito
 */
class ToggleBookmarkUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(materialId: String, userId: String, isBookmarked: Boolean): Result<Boolean> {
        // Verifica se o utilizador está autenticado
        val currentUser = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("Utilizador não autenticado"))

        // Verifica se o userId corresponde ao utilizador atual (segurança)
        if (currentUser.id != userId) {
            return Result.failure(Exception("Não autorizado"))
        }

        return materialRepository.toggleBookmark(materialId, userId, isBookmarked)
    }
}