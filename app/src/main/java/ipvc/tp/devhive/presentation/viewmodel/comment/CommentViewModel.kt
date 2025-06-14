package ipvc.tp.devhive.presentation.viewmodel.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.model.Attachment
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.domain.usecase.comment.CreateCommentUseCase
import ipvc.tp.devhive.domain.usecase.comment.GetCommentsUseCase
import ipvc.tp.devhive.domain.usecase.comment.LikeCommentUseCase
import ipvc.tp.devhive.domain.usecase.user.GetCurrentUserUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val likeCommentUseCase: LikeCommentUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _commentEvent = MutableLiveData<Event<CommentEvent>>()
    val commentEvent: LiveData<Event<CommentEvent>> = _commentEvent

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Obtém os comentários de um material específico
     */
    fun getCommentsByMaterial(materialId: String): LiveData<List<Comment>> {
        return getCommentsUseCase.byMaterial(materialId)
    }

    /**
     * Obtém as respostas de um comentário pai
     */
    fun getRepliesByParentId(parentId: String): LiveData<List<Comment>> {
        return getCommentsUseCase.repliesByParentId(parentId)
    }

    /**
     * Cria um novo comentário
     */
    fun createComment(
        materialId: String,
        userUid: String,
        content: String,
        parentCommentId: String? = null,
        attachments: List<Attachment> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = createCommentUseCase(
                materialId = materialId,
                userId = userUid,
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

            _isLoading.value = false
        }
    }

    /**
     * Curte/descurte um comentário
     */
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