package ipvc.tp.devhive.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ipvc.tp.devhive.data.local.dao.CommentDao
import ipvc.tp.devhive.data.local.entity.CommentEntity
import ipvc.tp.devhive.data.model.Comment
import ipvc.tp.devhive.data.remote.service.CommentService
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentRepository(
    private val commentDao: CommentDao,
    private val commentService: CommentService,
    private val appScope: CoroutineScope
) {
    fun getCommentsByMaterial(materialId: String): LiveData<List<Comment>> {
        appScope.launch {
            refreshCommentsByMaterial(materialId)
        }

        return commentDao.getCommentsByMaterial(materialId).map { entities ->
            entities.map { CommentEntity.toComment(it) }
        }
    }

    private suspend fun refreshCommentsByMaterial(materialId: String) {
        withContext(Dispatchers.IO) {
            val result = commentService.getCommentsByMaterial(materialId)
            if (result.isSuccess) {
                val comments = result.getOrThrow()
                val entities = comments.map { CommentEntity.fromComment(it) }
                commentDao.insertComments(entities)
            }
        }
    }

    suspend fun getCommentById(commentId: String): Comment? {
        return withContext(Dispatchers.IO) {
            val localComment = commentDao.getCommentById(commentId)

            if (localComment != null) {
                CommentEntity.toComment(localComment)
            } else {
                val remoteComment = commentService.getCommentById(commentId)

                if (remoteComment != null) {
                    commentDao.insertComment(CommentEntity.fromComment(remoteComment))
                }

                remoteComment
            }
        }
    }

    fun getRepliesByParentId(parentId: String): LiveData<List<Comment>> {
        appScope.launch {
            refreshRepliesByParentId(parentId)
        }

        return commentDao.getRepliesByParentId(parentId).map { entities ->
            entities.map { CommentEntity.toComment(it) }
        }
    }

    private suspend fun refreshRepliesByParentId(parentId: String) {
        withContext(Dispatchers.IO) {
            val result = commentService.getRepliesByParentId(parentId)
            if (result.isSuccess) {
                val replies = result.getOrThrow()
                val entities = replies.map { CommentEntity.fromComment(it) }
                commentDao.insertComments(entities)
            }
        }
    }

    suspend fun createComment(comment: Comment): Result<Comment> {
        return withContext(Dispatchers.IO) {
            try {
                val result = commentService.createComment(comment)

                if (result.isSuccess) {
                    val createdComment = result.getOrThrow()
                    commentDao.insertComment(CommentEntity.fromComment(createdComment))
                    Result.success(createdComment)
                } else {
                    val commentEntity = CommentEntity.fromComment(comment, SyncStatus.PENDING_UPLOAD)
                    commentDao.insertComment(commentEntity)
                    Result.success(comment)
                }
            } catch (e: Exception) {
                val commentEntity = CommentEntity.fromComment(comment, SyncStatus.PENDING_UPLOAD)
                commentDao.insertComment(commentEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun updateComment(comment: Comment): Result<Comment> {
        return withContext(Dispatchers.IO) {
            try {
                val result = commentService.updateComment(comment)

                if (result.isSuccess) {
                    commentDao.insertComment(CommentEntity.fromComment(comment))
                    Result.success(comment)
                } else {
                    val commentEntity = CommentEntity.fromComment(comment, SyncStatus.PENDING_UPDATE)
                    commentDao.insertComment(commentEntity)
                    Result.success(comment)
                }
            } catch (e: Exception) {
                val commentEntity = CommentEntity.fromComment(comment, SyncStatus.PENDING_UPDATE)
                commentDao.insertComment(commentEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteComment(commentId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = commentService.deleteComment(commentId)

                if (result.isSuccess) {
                    commentDao.deleteCommentById(commentId)
                    Result.success(true)
                } else {
                    val comment = commentDao.getCommentById(commentId)
                    if (comment != null) {
                        val updatedComment = comment.copy(syncStatus = SyncStatus.PENDING_DELETE)
                        commentDao.updateComment(updatedComment)
                    }
                    Result.success(false)
                }
            } catch (e: Exception) {
                val comment = commentDao.getCommentById(commentId)
                if (comment != null) {
                    val updatedComment = comment.copy(syncStatus = SyncStatus.PENDING_DELETE)
                    commentDao.updateComment(updatedComment)
                }
                Result.failure(e)
            }
        }
    }

    suspend fun likeComment(commentId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = commentService.likeComment(commentId)

                if (result.isSuccess) {
                    val comment = commentDao.getCommentById(commentId)
                    if (comment != null) {
                        val updatedComment = comment.copy(likes = comment.likes + 1)
                        commentDao.updateComment(updatedComment)
                    }
                }

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun syncPendingComments() {
        withContext(Dispatchers.IO) {
            val unsyncedComments = commentDao.getUnsyncedComments()

            for (commentEntity in unsyncedComments) {
                when (commentEntity.syncStatus) {
                    SyncStatus.PENDING_UPLOAD -> {
                        val comment = CommentEntity.toComment(commentEntity)
                        val result = commentService.createComment(comment)
                        if (result.isSuccess) {
                            commentDao.updateSyncStatus(commentEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_UPDATE -> {
                        val comment = CommentEntity.toComment(commentEntity)
                        val result = commentService.updateComment(comment)
                        if (result.isSuccess) {
                            commentDao.updateSyncStatus(commentEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_DELETE -> {
                        val result = commentService.deleteComment(commentEntity.id)
                        if (result.isSuccess) {
                            commentDao.deleteCommentById(commentEntity.id)
                        }
                    }
                }
            }
        }
    }
}
