package ipvc.tp.devhive.data.remote.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import ipvc.tp.devhive.data.model.GroupMessage
import ipvc.tp.devhive.data.model.StudyGroup
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StudyGroupService(firestore: FirebaseFirestore) {
    private val studyGroupsCollection = firestore.collection("studyGroups")

    suspend fun getStudyGroupById(studyGroupId: String): StudyGroup? {
        return try {
            val document = studyGroupsCollection.document(studyGroupId).get().await()
            if (document.exists()) {
                document.toObject(StudyGroup::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getStudyGroupsByUser(userId: String): Result<List<StudyGroup>> {
        return try {
            val snapshot = studyGroupsCollection
                .whereArrayContains("members", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val studyGroups = snapshot.documents.mapNotNull { it.toObject(StudyGroup::class.java) }
            Result.success(studyGroups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createStudyGroup(studyGroup: StudyGroup): Result<StudyGroup> {
        return try {
            val studyGroupId = studyGroup.id.ifEmpty { UUID.randomUUID().toString() }
            val newStudyGroup = studyGroup.copy(id = studyGroupId)
            studyGroupsCollection.document(studyGroupId).set(newStudyGroup).await()
            Result.success(newStudyGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStudyGroup(studyGroup: StudyGroup): Result<StudyGroup> {
        return try {
            studyGroupsCollection.document(studyGroup.id).set(studyGroup, SetOptions.merge()).await()
            Result.success(studyGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStudyGroup(studyGroupId: String): Result<Boolean> {
        return try {
            studyGroupsCollection.document(studyGroupId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMember(studyGroupId: String, userId: String): Result<Boolean> {
        return try {
            studyGroupsCollection.document(studyGroupId)
                .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(studyGroupId: String, userId: String): Result<Boolean> {
        return try {
            studyGroupsCollection.document(studyGroupId)
                .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupMessages(studyGroupId: String, limit: Long = 50): Result<List<GroupMessage>> {
        return try {
            val snapshot = studyGroupsCollection.document(studyGroupId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val messages = snapshot.documents.mapNotNull { it.toObject(GroupMessage::class.java) }
            Result.success(messages.reversed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendGroupMessage(studyGroupId: String, message: GroupMessage): Result<GroupMessage> {
        return try {
            val messageId = message.id.ifEmpty { UUID.randomUUID().toString() }
            val newMessage = message.copy(id = messageId, studyGroupId = studyGroupId)

            // Add message to subcollection
            studyGroupsCollection.document(studyGroupId)
                .collection("messages")
                .document(messageId)
                .set(newMessage)
                .await()

            // Update study group with last message info
            studyGroupsCollection.document(studyGroupId)
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

    suspend fun searchPublicStudyGroups(query: String): Result<List<StudyGroup>> {
        return try {
            val snapshot = studyGroupsCollection
                .whereEqualTo("isPrivate", false)
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            val studyGroups = snapshot.documents.mapNotNull { it.toObject(StudyGroup::class.java) }
            Result.success(studyGroups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
