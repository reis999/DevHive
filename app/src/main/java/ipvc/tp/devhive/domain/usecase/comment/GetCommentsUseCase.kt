package ipvc.tp.devhive.domain.usecase.comment

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.domain.repository.CommentRepository
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {

    fun byMaterial(materialId: String): LiveData<List<Comment>> {
        return commentRepository.getCommentsByMaterial(materialId)
    }

    fun repliesByParentId(parentId: String): LiveData<List<Comment>> {
        return commentRepository.getRepliesByParentId(parentId)
    }

    suspend fun byId(commentId: String): Comment? {
        return commentRepository.getCommentById(commentId)
    }
}