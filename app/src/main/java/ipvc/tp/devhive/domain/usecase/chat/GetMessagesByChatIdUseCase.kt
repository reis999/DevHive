package ipvc.tp.devhive.domain.usecase.chat

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Message
import ipvc.tp.devhive.domain.repository.ChatRepository
import javax.inject.Inject

class GetMessagesByChatIdUseCase @Inject constructor(
    private val repository: ChatRepository
)
{
    operator fun invoke(chatId: String): LiveData<List<Message>> {
        return repository.getMessagesByChatId(chatId)
    }
}