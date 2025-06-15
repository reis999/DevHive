package ipvc.tp.devhive.domain.usecase.sync

import ipvc.tp.devhive.domain.repository.ChatRepository
import ipvc.tp.devhive.domain.repository.CommentRepository
import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val materialRepository: MaterialRepository,
    private val commentRepository: CommentRepository,
    private val chatRepository: ChatRepository,
    private val studyGroupRepository: StudyGroupRepository
) {

    suspend operator fun invoke() {
        userRepository.syncPendingUsers()
        materialRepository.syncPendingMaterials()
        commentRepository.syncPendingComments()
        chatRepository.syncPendingChats()
        chatRepository.syncPendingMessages()
        studyGroupRepository.syncPendingStudyGroups()
    }
}
