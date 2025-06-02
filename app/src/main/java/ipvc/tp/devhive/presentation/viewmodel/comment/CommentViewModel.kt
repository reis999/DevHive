package ipvc.tp.devhive.presentation.viewmodel.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipvc.tp.devhive.domain.model.Attachment
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.domain.usecase.comment.CreateCommentUseCase
import ipvc.tp.devhive.domain.usecase.comment.LikeCommentUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch

class CommentViewModel(
    private val createCommentUseCase: CreateCommentUseCase,
    private val likeCommentUseCase: LikeCommentUseCase
) : ViewModel() {

    private val _commentEvent = MutableLiveData<Event<CommentEvent>>()
    val commentEvent: LiveData<Event<CommentEvent>> = _commentEvent

    fun createComment(
        materialId: String,
        userId: String,
        content: String,
        parentCommentId: String? = null,
        attachments: List<Attachment> = emptyList()
    ) {
        viewModelScope.launch {
            val result = createCommentUseCase(
                materialId = materialId,
                userId = userId,
                content = content,
                parentCommentId = parentCommentId,
                attachments = attachments
            )

            result.fold(
                onSuccess = {
                    _commentEvent.value = Event(CommentEvent.CreateSuccess(it))
                },
                onFailure = {
                    _commentEvent.value = Event(CommentEvent.CreateFailure(it.message ?: "Erro ao criar comentário"))
                }
            )
        }
    }

    fun likeComment(commentId: String, userId: String) {
        viewModelScope.launch {
            val result = likeCommentUseCase(commentId, userId)

            result.fold(
                onSuccess = {
                    _commentEvent.value = Event(CommentEvent.LikeSuccess(commentId))
                },
                onFailure = {
                    _commentEvent.value = Event(CommentEvent.LikeFailure(it.message ?: "Erro ao curtir comentário"))
                }
            )
        }
    }
}

sealed class CommentEvent {
    data class CreateSuccess(val comment: Comment) : CommentEvent()
    data class CreateFailure(val message: String) : CommentEvent()
    data class LikeSuccess(val commentId: String) : CommentEvent()
    data class LikeFailure(val message: String) : CommentEvent()
}
