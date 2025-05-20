package ipvc.tp.devhive.data.repository

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

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val chatService: ChatService,
    private val appScope: CoroutineScope
) {
    fun getChatsByUser(userId: String): LiveData<List<Chat>> {

        appScope.launch {
            refreshChatsByUser(userId)
        }

        return chatDao.getChatsByUser(userId).map { entities ->
            entities.map { ChatEntity.toChat(it) }
        }
    }

    private suspend fun refreshChatsByUser(userId: String) {
        withContext(Dispatchers.IO) {
            val result = chatService.getChatsByUser(userId)
            if (result.isSuccess) {
                val chats = result.getOrThrow()
                val entities = chats.map { ChatEntity.fromChat(it) }
                chatDao.insertChats(entities)
            }
        }
    }

    suspend fun getChatById(chatId: String): Chat? {
        return withContext(Dispatchers.IO) {

            val localChat = chatDao.getChatById(chatId)

            if (localChat != null) {
                ChatEntity.toChat(localChat)
            } else {

                val remoteChat = chatService.getChatById(chatId)

                if (remoteChat != null) {
                    chatDao.insertChat(ChatEntity.fromChat(remoteChat))
                }

                remoteChat
            }
        }
    }

    fun observeChatById(chatId: String): LiveData<Chat?> {
        appScope.launch {
            refreshChat(chatId)
        }

        return chatDao.observeChatById(chatId).map { entity ->
            entity?.let { ChatEntity.toChat(it) }
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

    suspend fun createChat(chat: Chat): Result<Chat> {
        return withContext(Dispatchers.IO) {
            try {
                val result = chatService.createChat(chat)

                if (result.isSuccess) {
                    val createdChat = result.getOrThrow()
                    chatDao.insertChat(ChatEntity.fromChat(createdChat))
                    Result.success(createdChat)
                } else {
                    val chatEntity = ChatEntity.fromChat(chat, SyncStatus.PENDING_UPLOAD)
                    chatDao.insertChat(chatEntity)
                    Result.success(chat)
                }
            } catch (e: Exception) {
                val chatEntity = ChatEntity.fromChat(chat, SyncStatus.PENDING_UPLOAD)
                chatDao.insertChat(chatEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun updateChat(chat: Chat): Result<Chat> {
        return withContext(Dispatchers.IO) {
            try {
                val result = chatService.updateChat(chat)

                if (result.isSuccess) {
                    chatDao.insertChat(ChatEntity.fromChat(chat))
                    Result.success(chat)
                } else {
                    val chatEntity = ChatEntity.fromChat(chat, SyncStatus.PENDING_UPDATE)
                    chatDao.insertChat(chatEntity)
                    Result.success(chat)
                }
            } catch (e: Exception) {
                val chatEntity = ChatEntity.fromChat(chat, SyncStatus.PENDING_UPDATE)
                chatDao.insertChat(chatEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteChat(chatId: String): Result<Boolean> {
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

    suspend fun addParticipant(chatId: String, userId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = chatService.addParticipant(chatId, userId)

                if (result.isSuccess) {
                    val chat = chatDao.getChatById(chatId)
                    if (chat != null) {
                        val updatedParticipants = chat.participantIds.toMutableList()
                        if (!updatedParticipants.contains(userId)) {
                            updatedParticipants.add(userId)
                        }
                        val updatedChat = chat.copy(participantIds = updatedParticipants)
                        chatDao.updateChat(updatedChat)
                    }
                }

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun removeParticipant(chatId: String, userId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = chatService.removeParticipant(chatId, userId)

                if (result.isSuccess) {
                    val chat = chatDao.getChatById(chatId)
                    if (chat != null) {
                        val updatedParticipants = chat.participantIds.toMutableList()
                        updatedParticipants.remove(userId)
                        val updatedChat = chat.copy(participantIds = updatedParticipants)
                        chatDao.updateChat(updatedChat)
                    }
                }

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getMessagesByChatId(chatId: String): LiveData<List<Message>> {
        appScope.launch {
            refreshMessagesByChatId(chatId)
        }

        return messageDao.getMessagesByChatId(chatId).map { entities ->
            entities.map { MessageEntity.toMessage(it) }
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

    suspend fun sendMessage(chatId: String, message: Message): Result<Message> {
        return withContext(Dispatchers.IO) {
            try {
                val result = chatService.sendMessage(chatId, message)

                if (result.isSuccess) {
                    val sentMessage = result.getOrThrow()
                    messageDao.insertMessage(MessageEntity.fromMessage(sentMessage))
                    Result.success(sentMessage)
                } else {
                    val messageEntity = MessageEntity.fromMessage(message, SyncStatus.PENDING_UPLOAD)
                    messageDao.insertMessage(messageEntity)
                    Result.success(message)
                }
            } catch (e: Exception) {
                val messageEntity = MessageEntity.fromMessage(message, SyncStatus.PENDING_UPLOAD)
                messageDao.insertMessage(messageEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun syncPendingChats() {
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

    suspend fun syncPendingMessages() {
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
                }
            }
        }
    }
}
