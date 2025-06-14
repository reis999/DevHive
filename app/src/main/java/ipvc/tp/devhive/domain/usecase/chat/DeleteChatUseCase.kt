package ipvc.tp.devhive.domain.usecase.chat

import ipvc.tp.devhive.domain.repository.ChatRepository
import javax.inject.Inject

class DeleteChatUseCase @Inject constructor (
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String): Result<Boolean> {
        return repository.deleteChat(chatId)
    }
}