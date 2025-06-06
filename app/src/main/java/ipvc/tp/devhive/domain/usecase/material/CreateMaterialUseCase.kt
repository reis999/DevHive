package ipvc.tp.devhive.domain.usecase.material

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject


/**
 * Caso de uso para criar um novo material de estudo
 */
class CreateMaterialUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        title: String,
        description: String,
        type: String,
        contentUrl: String,
        thumbnailUrl: String,
        fileSize: Long,
        ownerUid: String,
        ownerName: String,
        ownerImageUrl: String,
        categories: List<String>,
        isPublic: Boolean,
        subject: String
    ): Result<Material> {
        // Validação de dados
        if (title.isBlank() || description.isBlank() || type.isBlank() || subject.isBlank()) {
            return Result.failure(IllegalArgumentException("Campos obrigatórios não preenchidos"))
        }

        // Criação do material
        val materialId = UUID.randomUUID().toString()
        val now = Timestamp(Date())

        val newMaterial = Material(
            id = materialId,
            title = title,
            description = description,
            type = type,
            contentUrl = contentUrl,
            thumbnailUrl = thumbnailUrl,
            fileSize = fileSize,
            ownerUid = ownerUid,
            ownerName = ownerName,
            ownerImageUrl = ownerImageUrl,
            createdAt = now,
            updatedAt = now,
            categories = categories,
            isPublic = isPublic,
            subject = subject,
            views = 0,
            likes = 0,
            downloads = 0,
            bookmarked = false,
            rating = 0f,
            reviewCount = 0
        )

        // Atualiza as estatísticas de contribuição do utilizador
        val user = userRepository.getUserById(ownerUid)
        if (user != null) {
            val updatedStats = user.contributionStats.copy(
                materials = user.contributionStats.materials + 1
            )
            val updatedUser = user.copy(contributionStats = updatedStats)
            userRepository.updateUser(updatedUser)
        }

        return materialRepository.createMaterial(newMaterial)
    }
}
