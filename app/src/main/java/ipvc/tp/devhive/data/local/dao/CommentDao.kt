package ipvc.tp.devhive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ipvc.tp.devhive.data.local.entity.CommentEntity
import ipvc.tp.devhive.data.util.SyncStatus

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE materialId = :materialId ORDER BY createdAt ASC")
    fun getCommentsByMaterial(materialId: String): LiveData<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: String): CommentEntity?

    @Query("SELECT * FROM comments WHERE parentCommentId = :parentId ORDER BY createdAt ASC")
    fun getRepliesByParentId(parentId: String): LiveData<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("SELECT * FROM comments WHERE syncStatus != :syncStatus")
    suspend fun getUnsyncedComments(syncStatus: String = SyncStatus.SYNCED): List<CommentEntity>

    @Query("UPDATE comments SET syncStatus = :newStatus WHERE id = :commentId")
    suspend fun updateSyncStatus(commentId: String, newStatus: String)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)
}

