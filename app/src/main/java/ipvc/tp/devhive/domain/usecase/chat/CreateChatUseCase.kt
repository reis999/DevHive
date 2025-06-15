package ipvc.tp.devhive.domain.usecase.chat

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.repository.ChatRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class CreateChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
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

        val currentUserData = userRepository.getUserById(currentUserId)
            ?: return Result.failure(IllegalArgumentException("Utilizador não encontrado"))

        val otherUser = userRepository.getUserById(otherUserId)
            ?: return Result.failure(IllegalArgumentException("Utilizador não encontrado"))

        // Criação do chat
        val chatId = UUID.randomUUID().toString()
        val now = Timestamp(Date())

        val newChat = Chat(
            id = chatId,
            participant1Id = currentUserId,
            participant1Name = currentUserData.name,
            participant2Id = otherUserId,
            otherParticipantId = otherUserId,
            otherParticipantName = otherUser.name,
            otherParticipantImageUrl = otherUser.profileImageUrl,
            otherParticipantOnline = otherUser.isOnline,
            createdAt = now,
            updatedAt = now,
            lastMessageAt = now,
            lastMessagePreview = "",
            messageCount = 0
        )

        return chatRepository.createChat(newChat)
    }
}
