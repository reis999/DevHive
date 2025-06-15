package ipvc.tp.devhive.domain.usecase.comment

import ipvc.tp.devhive.domain.repository.CommentRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import ipvc.tp.devhive.domain.usecase.user.StatsAction
import ipvc.tp.devhive.domain.usecase.user.UpdateUserStatsUseCase
import javax.inject.Inject

class LikeCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val updateUserStatsUseCase: UpdateUserStatsUseCase
) {

    suspend operator fun invoke(commentId: String, userId: String): Result<Boolean> {
        val result = commentRepository.likeComment(commentId)

        if (result.isSuccess) {
            try {
                val comment = commentRepository.getCommentById(commentId)
                if (comment != null) {
                    updateUserStatsUseCase(comment.userId, StatsAction.INCREMENT_LIKES)
                }
            } catch (e: Exception) {
                // Não falha se não conseguir atualizar stats
            }
        }

        return result
    }
}
