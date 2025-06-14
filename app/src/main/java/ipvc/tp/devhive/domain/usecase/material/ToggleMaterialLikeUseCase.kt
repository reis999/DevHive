package ipvc.tp.devhive.domain.usecase.material

import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import ipvc.tp.devhive.domain.usecase.user.UpdateUserStatsUseCase
import ipvc.tp.devhive.domain.usecase.user.StatsAction
import javax.inject.Inject

class ToggleMaterialLikeUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val userRepository: UserRepository,
    private val updateUserStatsUseCase: UpdateUserStatsUseCase
) {

    suspend operator fun invoke(materialId: String, userId: String, isLiked: Boolean): Result<Boolean> {
        return try {
            // 1. Atualiza o like do material
            val result = materialRepository.toggleMaterialLike(materialId, userId, isLiked)

            if (result.isSuccess) {
                updateAuthorStats(materialId, isLiked)
            }

            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateAuthorStats(materialId: String, isLiked: Boolean) {
        try {
            val material = materialRepository.getMaterialById(materialId)
            if (material != null) {
                val action = if (isLiked) {
                    StatsAction.INCREMENT_LIKES
                } else {
                    StatsAction.DECREMENT_LIKES
                }
                updateUserStatsUseCase(material.ownerUid, action)
            }
        } catch (e: Exception) {
        }
    }
}