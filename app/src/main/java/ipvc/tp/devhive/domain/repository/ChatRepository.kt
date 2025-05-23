package ipvc.tp.devhive.domain.repository

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface de repositório para operações relacionadas a chats e mensagens
 */
interface ChatRepository {
    fun getChatsByUser(userId: String): LiveData<List<Chat>>
    suspend fun getChatById(chatId: String): Chat?
    fun observeChatById(chatId: String): LiveData<Chat?>
    suspend fun createChat(chat: Chat): Result<Chat>
    suspend fun updateChat(chat: Chat): Result<Chat>
    suspend fun deleteChat(chatId: String): Result<Boolean>
    suspend fun addParticipant(chatId: String, userId: String): Result<Boolean>
    suspend fun removeParticipant(chatId: String, userId: String): Result<Boolean>
    fun getMessagesByChatId(chatId: String): LiveData<List<Message>>
    suspend fun sendMessage(chatId: String, message: Message): Result<Message>
    suspend fun syncPendingChats()
    suspend fun syncPendingMessages()
}
