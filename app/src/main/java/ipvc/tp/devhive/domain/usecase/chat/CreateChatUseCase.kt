package ipvc.tp.devhive.domain.usecase.chat

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.repository.ChatRepository
import java.util.Date
import java.util.UUID

/**
 * Caso de uso para criar um novo chat direto entre dois utilizadores
 */
class CreateChatUseCase(
    private val chatRepository: ChatRepository
) {

    suspend operator fun invoke(
        currentUserId: String,
        otherUserId: String
    ): Result<Chat> {
        // Validação de dados
        if (currentUserId.isBlank() || otherUserId.isBlank()) {
            return Result.failure(IllegalArgumentException("IDs de utilizador não podem estar vazios"))
        }

        if (currentUserId == otherUserId) {
            return Result.failure(IllegalArgumentException("Não é possível criar um chat consigo mesmo"))
        }

        // Criação do chat
        val chatId = UUID.randomUUID().toString()
        val now = Timestamp(Date())

        val newChat = Chat(
            id = chatId,
            participant1Id = currentUserId,
            participant2Id = otherUserId,
            createdAt = now,
            updatedAt = now,
            lastMessageAt = now,
            lastMessagePreview = "",
            messageCount = 0
        )

        return chatRepository.createChat(newChat)
    }
}
