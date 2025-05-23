package ipvc.tp.devhive.domain.usecase.sync

import ipvc.tp.devhive.domain.repository.ChatRepository
import ipvc.tp.devhive.domain.repository.CommentRepository
import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.UserRepository

/**
 * Caso de uso para sincronizar dados pendentes
 */
class SyncDataUseCase(
    private val userRepository: UserRepository,
    private val materialRepository: MaterialRepository,
    private val commentRepository: CommentRepository,
    private val chatRepository: ChatRepository
) {

    suspend operator fun invoke() {
        // Sincroniza os dados na ordem correta para evitar problemas de dependência
        userRepository.syncPendingUsers()
        materialRepository.syncPendingMaterials()
        commentRepository.syncPendingComments()
        chatRepository.syncPendingChats()
        chatRepository.syncPendingMessages()
    }
}
