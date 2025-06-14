package ipvc.tp.devhive.data.remote.service

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import ipvc.tp.devhive.data.model.User
import ipvc.tp.devhive.data.util.FirebaseAuthHelper
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserService(firestore: FirebaseFirestore) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> {
        return try {
            if (userIds.isEmpty()) {
                Result.success(emptyList())
            } else {
                val remoteUsers = mutableListOf<User>()
                userIds.chunked(30).forEach { chunk ->
                    if (chunk.isNotEmpty()) {
                        val querySnapshot = usersCollection
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()
                        val usersFromChunk = querySnapshot.documents.mapNotNull { document ->
                            document.toObject(User::class.java)?.copy(id = document.id)
                        }
                        remoteUsers.addAll(usersFromChunk)
                    }
                }
                Result.success(remoteUsers)
            }
        } catch (e: Exception) {
            Log.e("UserServiceImpl", "Error fetching users by IDs from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String, excludeUserId: String?): List<User> {
        return try {
            val querySnapshot = usersCollection
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .whereNotEqualTo("id", excludeUserId)
                .get()
                .await()
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(User::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createUser(user: User): Result<User> {
        return try {
            val userId = user.id.ifEmpty { UUID.randomUUID().toString() }
            val newUser = user.copy(id = userId)
            usersCollection.document(userId).set(newUser).await()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<User> {
        return try {
            usersCollection.document(user.id).set(user, SetOptions.merge()).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Boolean> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Result<Boolean> {
        return try {
            usersCollection.document(userId).update("online", isOnline).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        val currentUserId = FirebaseAuthHelper.getCurrentUserId()
        return if (currentUserId != null) {
            getUserById(currentUserId)
        } else {
            null
        }
    }
}
