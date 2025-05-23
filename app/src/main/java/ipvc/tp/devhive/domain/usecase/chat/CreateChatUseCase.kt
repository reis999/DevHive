package ipvc.tp.devhive.domain.usecase.chat

import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.repository.ChatRepository
import java.util.Date
import java.util.UUID

/**
 * Caso de uso para criar um novo chat
 */
class CreateChatUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(
        name: String,
        creatorUid: String,
        isPrivate: Boolean,
        initialParticipants: List<String> = emptyList()
    ): Result<Chat> {
        // Validação de dados
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("O nome do chat não pode estar vazio"))
        }

        // Criação do chat
        val chatId = UUID.randomUUID().toString()
        val now = Date()

        // Gera um código de acesso aleatório para chats privados
        val accessCode = if (isPrivate) {
            (1..6).map { ('0'..'9').random() }.joinToString("")
        } else {
            ""
        }

        // Garante que o criador está na lista de participantes
        val participants = initialParticipants.toMutableList()
        if (!participants.contains(creatorUid)) {
            participants.add(creatorUid)
        }

        val newChat = Chat(
            id = chatId,
            name = name,
            isPrivate = isPrivate,
            accessCode = accessCode,
            maxCapacity = 50,
            participantIds = participants,
            creatorUid = creatorUid,
            createdAt = now,
            updatedAt = now,
            lastMessageAt = now,
            lastMessagePreview = "",
            unreadCount = 0,
            recipientId = "",
            recipientName = "",
            recipientImageUrl = "",
            recipientOnline = false,
            messageCount = 0
        )

        return chatRepository.createChat(newChat)
    }
}
