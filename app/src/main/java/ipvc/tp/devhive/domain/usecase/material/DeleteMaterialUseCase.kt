package ipvc.tp.devhive.domain.usecase.material

import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import ipvc.tp.devhive.domain.usecase.user.UpdateUserStatsUseCase
import ipvc.tp.devhive.domain.usecase.user.StatsAction
import javax.inject.Inject

class DeleteMaterialUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val userRepository: UserRepository,
    private val updateUserStatsUseCase: UpdateUserStatsUseCase
) {

    suspend operator fun invoke(materialId: String): Result<Boolean> {
        try {
            val material = materialRepository.getMaterialById(materialId)
                ?: return Result.failure(Exception("Material não encontrado"))

            val currentUser = userRepository.getCurrentUser()
                ?: return Result.failure(Exception("Utilizador não autenticado"))

            if (material.ownerUid != currentUser.id) {
                return Result.failure(Exception("Não tens permissão para eliminar este material"))
            }

            val materialLikes = material.likes

            val deleteResult = materialRepository.deleteMaterial(materialId)

            if (deleteResult.isSuccess) {
                updateOwnerStats(currentUser.id, materialLikes)

                return Result.success(true)
            } else {
                return Result.failure(
                    deleteResult.exceptionOrNull() ?: Exception("Erro ao eliminar material")
                )
            }

        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun updateOwnerStats(ownerUid: String, materialLikes: Int) {
        try {
            // Decrementar materials
            updateUserStatsUseCase(ownerUid, StatsAction.DECREMENT_MATERIALS)

            // Decrementar likes (se houver)
            if (materialLikes > 0) {
                repeat(materialLikes) {
                    updateUserStatsUseCase(ownerUid, StatsAction.DECREMENT_LIKES)
                }
            }
        } catch (e: Exception) {
            println("Erro ao atualizar estatísticas do criador: ${e.message}")
        }
    }
}