package ipvc.tp.devhive.domain.usecase.studygroup

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.domain.model.MessageAttachment
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date
import java.util.UUID

class SendGroupMessageUseCase(
    private val studyGroupRepository: StudyGroupRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        groupId: String,
        content: String,
        attachments: List<MessageAttachment> = emptyList()
    ): Result<GroupMessage> {
        // Verifica se o usuário está logado
        val currentUser = userRepository.getCurrentUser() ?: return Result.failure(
            IllegalStateException("Usuário não está logado")
        )

        // Verifica se o grupo existe
        val group = studyGroupRepository.getStudyGroupById(groupId) ?: return Result.failure(
            IllegalArgumentException("Grupo não encontrado")
        )

        // Verifica se o usuário é membro do grupo
        if (!group.members.contains(currentUser.id)) {
            return Result.failure(IllegalStateException("Você não é membro deste grupo"))
        }

        // Cria o objeto GroupMessage
        val message = GroupMessage(
            id = UUID.randomUUID().toString(),
            studyGroupId = groupId,
            content = content,
            senderUid = currentUser.id,
            senderName = currentUser.name ?: "Usuário",
            senderImageUrl = currentUser.profileImageUrl ?: "",
            createdAt = Timestamp(Date()),
            attachments = attachments
        )

        // Envia a mensagem
        return studyGroupRepository.sendGroupMessage(groupId, message)
    }
}
