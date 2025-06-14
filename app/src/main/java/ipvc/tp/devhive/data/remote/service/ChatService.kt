package ipvc.tp.devhive.data.remote.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import ipvc.tp.devhive.data.model.Chat
import ipvc.tp.devhive.data.model.Message
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatService(firestore: FirebaseFirestore) {
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
            val snapshot1 = chatsCollection
                .whereEqualTo("participant1Id", userId)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val chatsFromQuery1 = snapshot1.documents.mapNotNull { doc ->
                doc.toObject(Chat::class.java)?.copy(id = doc.id)
            }

            val snapshot2 = chatsCollection
                .whereEqualTo("participant2Id", userId)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val chatsFromQuery2 = snapshot2.documents.mapNotNull { doc ->
                doc.toObject(Chat::class.java)?.copy(id = doc.id)
            }

            val combinedChats = (chatsFromQuery1 + chatsFromQuery2)
                .distinctBy { it.id }
                .sortedByDescending { it.lastMessageAt }

            Result.success(combinedChats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findChatBetweenUsers(user1Id: String, user2Id: String): Chat? {
        return try {
            val query1 = chatsCollection
                .whereEqualTo("participant1Id", user1Id)
                .whereEqualTo("participant2Id", user2Id)
                .get()
                .await()

            if (!query1.isEmpty) {
                return query1.documents[0].toObject(Chat::class.java)
            }

            val query2 = chatsCollection
                .whereEqualTo("participant1Id", user2Id)
                .whereEqualTo("participant2Id", user1Id)
                .get()
                .await()

            if (!query2.isEmpty) {
                return query2.documents[0].toObject(Chat::class.java)
            }

            null
        } catch (e: Exception) {
            null
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
