package ipvc.tp.devhive.domain.usecase.studygroup

import android.net.Uri
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject


class SendGroupMessageUseCase @Inject constructor(
    private val studyGroupRepository: StudyGroupRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        groupId: String,
        content: String,
        attachmentUri: Uri?,
        originalAttachmentFileName: String?
    ): Result<GroupMessage> {
        val currentUser = userRepository.getCurrentUser()
            ?: return Result.failure(IllegalStateException("Utilizador não está logado"))

        return studyGroupRepository.sendGroupMessage(
            groupId = groupId,
            messageContent = content,
            senderId = currentUser.id,
            senderName = currentUser.name,
            senderImageUrl = currentUser.profileImageUrl,
            attachmentUri = attachmentUri,
            originalAttachmentFileName = originalAttachmentFileName
        )
    }
}

