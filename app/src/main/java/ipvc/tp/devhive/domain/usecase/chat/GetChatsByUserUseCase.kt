package ipvc.tp.devhive.domain.usecase.chat

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.repository.ChatRepository
import javax.inject.Inject

class GetChatsByUserUseCase @Inject constructor(
    private val repository: ChatRepository
)
{
    operator fun invoke(userId: String): LiveData<List<Chat>> {
        return repository.getChatsByUser(userId)
    }
}