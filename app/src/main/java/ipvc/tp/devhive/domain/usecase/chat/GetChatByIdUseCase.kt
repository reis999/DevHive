package ipvc.tp.devhive.domain.usecase.chat

import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.repository.ChatRepository
import javax.inject.Inject

class GetChatByIdUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String): Chat? {
        return repository.getChatById(chatId)
    }
}