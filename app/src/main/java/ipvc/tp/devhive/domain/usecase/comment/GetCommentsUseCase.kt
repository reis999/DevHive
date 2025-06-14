package ipvc.tp.devhive.domain.usecase.comment

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.domain.repository.CommentRepository
import javax.inject.Inject

/**
 * Caso de uso para obter comentários
 */
class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {

    /**
     * Obtém todos os comentários de um material
     */
    fun byMaterial(materialId: String): LiveData<List<Comment>> {
        return commentRepository.getCommentsByMaterial(materialId)
    }

    /**
     * Obtém as respostas de um comentário pai
     */
    fun repliesByParentId(parentId: String): LiveData<List<Comment>> {
        return commentRepository.getRepliesByParentId(parentId)
    }

    /**
     * Obtém um comentário específico pelo ID
     */
    suspend fun byId(commentId: String): Comment? {
        return commentRepository.getCommentById(commentId)
    }
}