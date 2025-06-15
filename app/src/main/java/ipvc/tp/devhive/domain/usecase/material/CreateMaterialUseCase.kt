package ipvc.tp.devhive.domain.usecase.material

import android.net.Uri
import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import ipvc.tp.devhive.domain.usecase.user.StatsAction
import ipvc.tp.devhive.domain.usecase.user.UpdateUserStatsUseCase
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class CreateMaterialUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val userRepository: UserRepository,
    private val updateUserStatsUseCase: UpdateUserStatsUseCase

) {

    suspend operator fun invoke(
        title: String,
        description: String,
        type: String,
        subject: String,
        categories: List<String>,
        isPublic: Boolean,
        fileUri: Uri,
        thumbnailUri: Uri? = null
    ): Result<Material> {

        val currentUser = userRepository.getCurrentUser() ?: return Result.failure(
            IllegalStateException("Utilizador não está logado")
        )

        if (title.isBlank() || description.isBlank() || type.isBlank() || subject.isBlank()) {
            return Result.failure(IllegalArgumentException("Campos obrigatórios não preenchidos"))
        }

        val materialId = UUID.randomUUID().toString()
        val now = Timestamp(Date())

        val material = Material(
            id = materialId,
            title = title,
            description = description,
            type = type,
            subject = subject,
            contentUrl = "",
            thumbnailUrl = "",
            fileSize = 0,
            ownerUid = currentUser.id,
            ownerName = currentUser.name,
            ownerImageUrl = currentUser.profileImageUrl,
            createdAt = now,
            updatedAt = now,
            categories = categories,
            isPublic = isPublic,
            views = 0,
            likes = 0,
            downloads = 0,
            bookmarks = 0,
            bookmarkedBy = emptyList(),
            likedBy = emptyList(),
            rating = 0f,
            reviewCount = 0
        )

        val result = materialRepository.createMaterial(
            material = material,
            fileUri = fileUri,
            thumbnailUri = thumbnailUri
        )

        if (result.isSuccess) {
            updateUserStats(material.ownerUid)
        }

        return result
    }

    private suspend fun updateUserStats(ownerUid: String) {
        try {
            updateUserStatsUseCase(ownerUid, StatsAction.INCREMENT_MATERIALS)
        } catch (e: Exception) {
            println("Erro ao atualizar estatísticas do utilizador: ${e.message}")
        }
    }
}