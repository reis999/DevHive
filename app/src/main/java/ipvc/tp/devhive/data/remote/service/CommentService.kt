package ipvc.tp.devhive.data.remote.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import ipvc.tp.devhive.data.model.Comment
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommentService(firestore: FirebaseFirestore) {
    private val commentsCollection = firestore.collection("comments")

    suspend fun getCommentById(commentId: String): Comment? {
        return try {
            val document = commentsCollection.document(commentId).get().await()
            if (document.exists()) {
                document.toObject(Comment::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCommentsByMaterial(materialId: String): Result<List<Comment>> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("materialId", materialId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()

            val comments = snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRepliesByParentId(parentId: String): Result<List<Comment>> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("parentCommentId", parentId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()

            val replies = snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
            Result.success(replies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createComment(comment: Comment): Result<Comment> {
        return try {
            val commentId = comment.id.ifEmpty { UUID.randomUUID().toString() }
            val newComment = comment.copy(id = commentId)
            commentsCollection.document(commentId).set(newComment).await()
            Result.success(newComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateComment(comment: Comment): Result<Comment> {
        return try {
            commentsCollection.document(comment.id).set(comment, SetOptions.merge()).await()
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentId: String): Result<Boolean> {
        return try {
            commentsCollection.document(commentId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeComment(commentId: String): Result<Boolean> {
        return try {
            commentsCollection.document(commentId)
                .update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
