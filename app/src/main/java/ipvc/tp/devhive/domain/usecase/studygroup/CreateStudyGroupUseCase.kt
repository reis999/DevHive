package ipvc.tp.devhive.domain.usecase.studygroup

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject


class CreateStudyGroupUseCase @Inject constructor(
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
        val currentUser = userRepository.getCurrentUser() ?: return Result.failure(
            IllegalStateException("Utilizador não está logado")
        )

        val joinCode = if (isPrivate) generateJoinCode() else ""

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

        return studyGroupRepository.createStudyGroup(studyGroup)
    }

    private fun generateJoinCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
