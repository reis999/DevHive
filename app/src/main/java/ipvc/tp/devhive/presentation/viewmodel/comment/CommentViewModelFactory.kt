package ipvc.tp.devhive.presentation.viewmodel.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.domain.usecase.comment.CreateCommentUseCase
import ipvc.tp.devhive.domain.usecase.comment.LikeCommentUseCase

class CommentViewModelFactory(
    private val createCommentUseCase: CreateCommentUseCase,
    private val likeCommentUseCase: LikeCommentUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
            return CommentViewModel(
                createCommentUseCase,
                likeCommentUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
