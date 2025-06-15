package ipvc.tp.devhive.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ipvc.tp.devhive.data.local.dao.ChatDao
import ipvc.tp.devhive.data.local.dao.MessageDao
import ipvc.tp.devhive.data.local.entity.ChatEntity
import ipvc.tp.devhive.data.local.entity.MessageEntity
import ipvc.tp.devhive.data.model.Chat
import ipvc.tp.devhive.data.model.Message
import ipvc.tp.devhive.data.remote.service.ChatService
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ipvc.tp.devhive.domain.repository.ChatRepository as DomainChatRepository

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val chatService: ChatService,
    private val appScope: CoroutineScope
) : DomainChatRepository {

    override fun getChatsByUser(userId: String): LiveData<List<ipvc.tp.devhive.domain.model.Chat>> {
        appScope.launch {
            refreshChatsByUser(userId)
        }

        return chatDao.getChatsByUser(userId).map { entities ->
            entities.map { ChatEntity.toChat(it).toDomainChat() }
        }
    }

    private suspend fun refreshChatsByUser(userId: String) {
        withContext(Dispatchers.IO) {
            val result = chatService.getChatsByUser(userId)
            if (result.isSuccess) {
                val chats = result.getOrThrow()
                val entities = chats.map { ChatEntity.fromChat(it) }
                Log.d("ChatRepository", "Received ${entities.size} chats from Firestore")
                Log.d("ChatRepository", "Entities: $entities")
                chatDao.insertChats(entities)
            }
        }
    }

    override suspend fun getChatById(chatId: String): ipvc.tp.devhive.domain.model.Chat? {
        return withContext(Dispatchers.IO) {
            val localChat = chatDao.getChatById(chatId)

            if (localChat != null) {
                ChatEntity.toChat(localChat).toDomainChat()
            } else {
                val remoteChat = chatService.getChatById(chatId)

                if (remoteChat != null) {
                    chatDao.insertChat(ChatEntity.fromChat(remoteChat))
                    remoteChat.toDomainChat()
                } else {
                    null
                }
            }
        }
    }

    override fun observeChatById(chatId: String): LiveData<ipvc.tp.devhive.domain.model.Chat?> {
        appScope.launch {
            refreshChat(chatId)
        }

        return chatDao.observeChatById(chatId).map { entity ->
            entity?.let { ChatEntity.toChat(it).toDomainChat() }
        }
    }

    private suspend fun refreshChat(chatId: String) {
        withContext(Dispatchers.IO) {
            val remoteChat = chatService.getChatById(chatId)
            if (remoteChat != null) {
                chatDao.insertChat(ChatEntity.fromChat(remoteChat))
            }
        }
    }

    override suspend fun createChat(chat: ipvc.tp.devhive.domain.model.Chat): Result<ipvc.tp.devhive.domain.model.Chat> {
        return withContext(Dispatchers.IO) {
            try {
                val dataChat = chat.toDataChat()
                val existingChat = chatService.findChatBetweenUsers(dataChat.participant1Id, dataChat.participant2Id)
                if (existingChat != null) {
                    chatDao.insertChat(ChatEntity.fromChat(existingChat))
                    return@withContext Result.success(existingChat.toDomainChat())
                }

                val result = chatService.createChat(dataChat)

                if (result.isSuccess) {
                    val createdChat = result.getOrThrow()
                    chatDao.insertChat(ChatEntity.fromChat(createdChat))
                    Result.success(createdChat.toDomainChat())
                } else {
                    val chatEntity = ChatEntity.fromChat(dataChat, SyncStatus.PENDING_UPLOAD)
                    chatDao.insertChat(chatEntity)
                    Result.success(chat)
                }
            } catch (e: Exception) {
                val chatEntity = ChatEntity.fromChat(chat.toDataChat(), SyncStatus.PENDING_UPLOAD)
                chatDao.insertChat(chatEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun updateChat(chat: ipvc.tp.devhive.domain.model.Chat): Result<ipvc.tp.devhive.domain.model.Chat> {
        return withContext(Dispatchers.IO) {
            try {
                val dataChat = chat.toDataChat()
                val result = chatService.updateChat(dataChat)

                if (result.isSuccess) {
                    chatDao.insertChat(ChatEntity.fromChat(dataChat))
                    Result.success(chat)
                } else {
                    val chatEntity = ChatEntity.fromChat(dataChat, SyncStatus.PENDING_UPDATE)
                    chatDao.insertChat(chatEntity)
                    Result.success(chat)
                }
            } catch (e: Exception) {
                val chatEntity = ChatEntity.fromChat(chat.toDataChat(), SyncStatus.PENDING_UPDATE)
                chatDao.insertChat(chatEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteChat(chatId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = chatService.deleteChat(chatId)

                if (result.isSuccess) {
                    chatDao.deleteChatById(chatId)
                    Result.success(true)
                } else {
                    val chat = chatDao.getChatById(chatId)
                    if (chat != null) {
                        val updatedChat = chat.copy(syncStatus = SyncStatus.PENDING_DELETE)
                        chatDao.updateChat(updatedChat)
                    }
                    Result.success(false)
                }
            } catch (e: Exception) {
                val chat = chatDao.getChatById(chatId)
                if (chat != null) {
                    val updatedChat = chat.copy(syncStatus = SyncStatus.PENDING_DELETE)
                    chatDao.updateChat(updatedChat)
                }
                Result.failure(e)
            }
        }
    }

    override fun getMessagesByChatId(chatId: String): LiveData<List<ipvc.tp.devhive.domain.model.Message>> {
        appScope.launch {
            refreshMessagesByChatId(chatId)
        }

        return messageDao.getMessagesByChatId(chatId).map { entities ->
            entities.map { MessageEntity.toMessage(it).toDomainMessage() }
        }
    }

    private suspend fun refreshMessagesByChatId(chatId: String) {
        withContext(Dispatchers.IO) {
            val result = chatService.getMessagesByChatId(chatId)
            if (result.isSuccess) {
                val messages = result.getOrThrow()
                val entities = messages.map { MessageEntity.fromMessage(it) }
                messageDao.insertMessages(entities)
            }
        }
    }

    override suspend fun sendMessage(chatId: String, message: ipvc.tp.devhive.domain.model.Message): Result<ipvc.tp.devhive.domain.model.Message> {
        return withContext(Dispatchers.IO) {
            try {
                val dataMessage = message.toDataMessage()
                val result = chatService.sendMessage(chatId, dataMessage)

                if (result.isSuccess) {
                    val sentMessage = result.getOrThrow()
                    messageDao.insertMessage(MessageEntity.fromMessage(sentMessage))
                    Result.success(sentMessage.toDomainMessage())
                } else {
                    val messageEntity = MessageEntity.fromMessage(dataMessage, SyncStatus.PENDING_UPLOAD)
                    messageDao.insertMessage(messageEntity)
                    Result.success(message)
                }
            } catch (e: Exception) {
                val messageEntity = MessageEntity.fromMessage(message.toDataMessage(), SyncStatus.PENDING_UPLOAD)
                messageDao.insertMessage(messageEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun syncPendingChats() {
        withContext(Dispatchers.IO) {
            val unsyncedChats = chatDao.getUnsyncedChats()

            for (chatEntity in unsyncedChats) {
                when (chatEntity.syncStatus) {
                    SyncStatus.PENDING_UPLOAD -> {
                        val chat = ChatEntity.toChat(chatEntity)
                        val result = chatService.createChat(chat)
                        if (result.isSuccess) {
                            chatDao.updateSyncStatus(chatEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_UPDATE -> {
                        val chat = ChatEntity.toChat(chatEntity)
                        val result = chatService.updateChat(chat)
                        if (result.isSuccess) {
                            chatDao.updateSyncStatus(chatEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_DELETE -> {
                        val result = chatService.deleteChat(chatEntity.id)
                        if (result.isSuccess) {
                            chatDao.deleteChatById(chatEntity.id)
                        }
                    }
                }
            }
        }
    }

    override suspend fun syncPendingMessages() {
        withContext(Dispatchers.IO) {
            val unsyncedMessages = messageDao.getUnsyncedMessages()

            for (messageEntity in unsyncedMessages) {
                when (messageEntity.syncStatus) {
                    SyncStatus.PENDING_UPLOAD -> {
                        val message = MessageEntity.toMessage(messageEntity)
                        val result = chatService.sendMessage(messageEntity.chatId, message)
                        if (result.isSuccess) {
                            messageDao.updateSyncStatus(messageEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    // Mensagens geralmente não são atualizadas ou excluídas, mas poderíamos implementar se necessário
                }
            }
        }
    }

    // Funções de extensão para converter entre modelos data e domain
    private fun Chat.toDomainChat(): ipvc.tp.devhive.domain.model.Chat {
        return ipvc.tp.devhive.domain.model.Chat(
            id = this.id,
            participant1Id = this.participant1Id,
            participant1Name = this.participant1Name,
            participant2Id = this.participant2Id,
            lastMessagePreview = this.lastMessagePreview,
            lastMessageAt = this.lastMessageAt,
            unreadCount = this.unreadCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            otherParticipantId = this.otherParticipantId,
            otherParticipantName = this.otherParticipantName,
            otherParticipantImageUrl = this.otherParticipantImageUrl,
            messageCount = this.messageCount
        )
    }

    private fun ipvc.tp.devhive.domain.model.Chat.toDataChat(): Chat {
        return Chat(
            id = this.id,
            participant1Id = this.participant1Id,
            participant1Name = this.participant1Name,
            participant2Id = this.participant2Id,
            lastMessagePreview = this.lastMessagePreview,
            lastMessageAt = this.lastMessageAt,
            unreadCount = this.unreadCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            otherParticipantId = this.otherParticipantId,
            otherParticipantName = this.otherParticipantName,
            otherParticipantImageUrl = this.otherParticipantImageUrl,
            messageCount = this.messageCount
        )
    }

    private fun Message.toDomainMessage(): ipvc.tp.devhive.domain.model.Message {
        return ipvc.tp.devhive.domain.model.Message(
            id = this.id,
            chatId = this.chatId,
            senderUid = this.senderUid,
            content = this.content,
            attachments = this.attachments.toDomainList(),
            createdAt = this.createdAt,
            read = this.read,
            syncStatus = this.syncStatus,
            lastSync = this.lastSync
        )
    }

    private fun ipvc.tp.devhive.domain.model.Message.toDataMessage(): Message {
        return Message(
            id = this.id,
            chatId = this.chatId,
            senderUid = this.senderUid,
            content = this.content,
            attachments = this.attachments.toDataList(),
            createdAt = this.createdAt,
            read = this.read,
            syncStatus = this.syncStatus,
            lastSync = this.lastSync
        )
    }

    private fun ipvc.tp.devhive.data.model.MessageAttachment.toDomain(): ipvc.tp.devhive.domain.model.MessageAttachment {
        return ipvc.tp.devhive.domain.model.MessageAttachment(
            id = this.id,
            type = this.type,
            url = this.url,
            name = this.name,
            size = this.size,
            fileExtension = this.fileExtension
        )
    }

    private fun ipvc.tp.devhive.domain.model.MessageAttachment.toData(): ipvc.tp.devhive.data.model.MessageAttachment {
        return ipvc.tp.devhive.data.model.MessageAttachment(
            id = this.id,
            type = this.type,
            url = this.url,
            name = this.name,
            size = this.size,
            fileExtension =  this.fileExtension
        )
    }

    private fun List<ipvc.tp.devhive.data.model.MessageAttachment>.toDomainList(): List<ipvc.tp.devhive.domain.model.MessageAttachment> =
        map { it.toDomain() }

    private fun List<ipvc.tp.devhive.domain.model.MessageAttachment>.toDataList(): List<ipvc.tp.devhive.data.model.MessageAttachment> =
        map { it.toData() }

}
