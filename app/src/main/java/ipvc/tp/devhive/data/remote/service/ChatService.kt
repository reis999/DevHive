package ipvc.tp.devhive.data.remote.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import ipvc.tp.devhive.data.model.Chat
import ipvc.tp.devhive.data.model.Message
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatService(private val firestore: FirebaseFirestore) {
    private val chatsCollection = firestore.collection("chats")

    suspend fun getChatById(chatId: String): Chat? {
        return try {
            val document = chatsCollection.document(chatId).get().await()
            if (document.exists()) {
                document.toObject(Chat::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getChatsByUser(userId: String): Result<List<Chat>> {
        return try {
            val snapshot = chatsCollection
                .whereArrayContains("participantIds", userId)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val chats = snapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
            Result.success(chats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createChat(chat: Chat): Result<Chat> {
        return try {
            val chatId = chat.id.ifEmpty { UUID.randomUUID().toString() }
            val newChat = chat.copy(id = chatId)
            chatsCollection.document(chatId).set(newChat).await()
            Result.success(newChat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChat(chat: Chat): Result<Chat> {
        return try {
            chatsCollection.document(chat.id).set(chat, SetOptions.merge()).await()
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChat(chatId: String): Result<Boolean> {
        return try {
            chatsCollection.document(chatId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addParticipant(chatId: String, userId: String): Result<Boolean> {
        return try {
            chatsCollection.document(chatId)
                .update("participantIds", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeParticipant(chatId: String, userId: String): Result<Boolean> {
        return try {
            chatsCollection.document(chatId)
                .update("participantIds", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessagesByChatId(chatId: String, limit: Long = 50): Result<List<Message>> {
        return try {
            val snapshot = chatsCollection.document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
            Result.success(messages.reversed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatId: String, message: Message): Result<Message> {
        return try {
            val messageId = message.id.ifEmpty { UUID.randomUUID().toString() }
            val newMessage = message.copy(id = messageId, chatId = chatId)

            // Add message to subcollection
            chatsCollection.document(chatId)
                .collection("messages")
                .document(messageId)
                .set(newMessage)
                .await()

            // Update chat with last message info
            chatsCollection.document(chatId)
                .update(
                    mapOf(
                        "lastMessageAt" to newMessage.createdAt,
                        "lastMessagePreview" to newMessage.content.take(50),
                        "messageCount" to com.google.firebase.firestore.FieldValue.increment(1)
                    )
                )
                .await()

            Result.success(newMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
