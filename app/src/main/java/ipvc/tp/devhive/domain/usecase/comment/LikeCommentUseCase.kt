package ipvc.tp.devhive.domain.usecase.comment

import ipvc.tp.devhive.domain.repository.CommentRepository
import ipvc.tp.devhive.domain.repository.UserRepository

/**
 * Caso de uso para curtir um comentário
 */
class LikeCommentUseCase(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(commentId: String, userId: String): Result<Boolean> {
        val result = commentRepository.likeComment(commentId)

        if (result.isSuccess) {
            // Atualiza as estatísticas de contribuição do autor do comentário
            val comment = commentRepository.getCommentById(commentId)
            if (comment != null) {
                val authorId = comment.userId
                val author = userRepository.getUserById(authorId)

                if (author != null) {
                    val updatedStats = author.contributionStats.copy(
                        likes = author.contributionStats.likes + 1
                    )
                    val updatedUser = author.copy(contributionStats = updatedStats)
                    userRepository.updateUser(updatedUser)
                }
            }
        }

        return result
    }
}
