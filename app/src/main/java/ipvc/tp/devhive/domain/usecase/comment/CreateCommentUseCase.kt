package ipvc.tp.devhive.domain.usecase.comment

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.Attachment
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.domain.repository.CommentRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import ipvc.tp.devhive.domain.usecase.user.StatsAction
import ipvc.tp.devhive.domain.usecase.user.UpdateUserStatsUseCase
import java.util.UUID
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val updateUserStatsUseCase: UpdateUserStatsUseCase
) {

    suspend operator fun invoke(
        materialId: String,
        userId: String,
        content: String,
        parentCommentId: String? = null,
        attachments: List<Attachment> = emptyList()
    ): Result<Comment> {
        // Validação de dados
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("O conteúdo do comentário não pode estar vazio"))
        }

        // Busca informações do utilizador
        val user = userRepository.getUserById(userId)
            ?: return Result.failure(IllegalArgumentException("Utilizador não encontrado"))

        // Criação do comentário
        val commentId = UUID.randomUUID().toString()
        val now = Timestamp.now()

        val newComment = Comment(
            id = commentId,
            materialId = materialId,
            userId = userId,
            userName = user.name,
            userImageUrl = user.profileImageUrl,
            content = content,
            createdAt = now,
            updatedAt = now,
            likes = 0,
            liked = false,
            parentCommentId = parentCommentId,
            attachments = attachments
        )

        try {
            updateUserStatsUseCase(userId, StatsAction.INCREMENT_COMMENTS)
        } catch (e: Exception) {
            // Não falha criação do comentário se não conseguir atualizar stats
        }

        return commentRepository.createComment(newComment)
    }
}

