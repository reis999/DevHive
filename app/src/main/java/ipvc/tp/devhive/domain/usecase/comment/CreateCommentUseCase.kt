package ipvc.tp.devhive.domain.usecase.comment

import ipvc.tp.devhive.domain.model.Attachment
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.domain.repository.CommentRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date
import java.util.UUID

/**
 * Caso de uso para criar um novo comentário
 */
class CreateCommentUseCase(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        materialId: String,
        userUid: String,
        content: String,
        parentCommentId: String? = null,
        attachments: List<Attachment> = emptyList()
    ): Result<Comment> {
        // Validação de dados
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("O conteúdo do comentário não pode estar vazio"))
        }

        // Criação do comentário
        val commentId = UUID.randomUUID().toString()
        val now = Date()

        val newComment = Comment(
            id = commentId,
            materialId = materialId,
            userUid = userUid,
            content = content,
            createdAt = now,
            updatedAt = now,
            likes = 0,
            parentCommentId = parentCommentId,
            attachments = attachments
        )

        // Atualiza as estatísticas de contribuição do usuário
        val user = userRepository.getUserById(userUid)
        if (user != null) {
            val updatedStats = user.contributionStats.copy(
                comments = user.contributionStats.comments + 1
            )
            val updatedUser = user.copy(contributionStats = updatedStats)
            userRepository.updateUser(updatedUser)
        }

        return commentRepository.createComment(newComment)
    }
}
