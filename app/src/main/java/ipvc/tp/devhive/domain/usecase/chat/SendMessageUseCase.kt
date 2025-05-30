package ipvc.tp.devhive.domain.usecase.chat

import com.google.firebase.Timestamp
import ipvc.tp.devhive.data.util.SyncStatus
import ipvc.tp.devhive.domain.model.Message
import ipvc.tp.devhive.domain.model.MessageAttachment
import ipvc.tp.devhive.domain.repository.ChatRepository
import java.util.Date
import java.util.UUID

/**
 * Caso de uso para enviar uma mensagem em um chat
 */
class SendMessageUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(
        chatId: String,
        senderUid: String,
        content: String,
        attachments: List<MessageAttachment> = emptyList()
    ): Result<Message> {
        // Validação de dados
        if (content.isBlank() && attachments.isEmpty()) {
            return Result.failure(IllegalArgumentException("A mensagem não pode estar vazia"))
        }

        // Criação da mensagem
        val messageId = UUID.randomUUID().toString()
        val now = Timestamp(Date())

        val newMessage = Message(
            id = messageId,
            chatId = chatId,
            content = content,
            senderUid = senderUid,
            createdAt = now,
            attachments = attachments,
            read = false,
            syncStatus = SyncStatus.SYNCED,
            lastSync = now
        )

        return chatRepository.sendMessage(chatId, newMessage)
    }
}
