package ipvc.tp.devhive.domain.usecase.studygroup

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date
import java.util.UUID

class CreateStudyGroupUseCase(
    private val studyGroupRepository: StudyGroupRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        categories: List<String>,
        isPrivate: Boolean,
        maxMembers: Int
    ): Result<StudyGroup> {
        // Verifica se o utilizador está logado
        val currentUser = userRepository.getCurrentUser() ?: return Result.failure(
            IllegalStateException("Usuário não está logado")
        )

        // Gera um código de acesso para grupos privados
        val joinCode = if (isPrivate) generateJoinCode() else ""

        // Cria o objeto StudyGroup
        val studyGroup = StudyGroup(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            createdBy = currentUser.id,
            createdAt = Timestamp(Date()),
            updatedAt = Timestamp(Date()),
            imageUrl = "",
            members = listOf(currentUser.id),
            admins = listOf(currentUser.id),
            categories = categories,
            isPrivate = isPrivate,
            joinCode = joinCode,
            maxMembers = maxMembers,
            lastMessageAt = null,
            lastMessagePreview = "",
            messageCount = 0
        )

        // Salva o grupo no repositório
        return studyGroupRepository.createStudyGroup(studyGroup)
    }

    private fun generateJoinCode(): String {
        // Gera um código de 6 caracteres alfanuméricos
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
