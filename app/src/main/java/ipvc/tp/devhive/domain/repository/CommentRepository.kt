package ipvc.tp.devhive.domain.repository

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getCommentsByMaterial(materialId: String): LiveData<List<Comment>>
    suspend fun getCommentById(commentId: String): Comment?
    fun getRepliesByParentId(parentId: String): LiveData<List<Comment>>
    suspend fun createComment(comment: Comment): Result<Comment>
    suspend fun updateComment(comment: Comment): Result<Comment>
    suspend fun deleteComment(commentId: String): Result<Boolean>
    suspend fun likeComment(commentId: String): Result<Boolean>
    suspend fun syncPendingComments()
}
