package ipvc.tp.devhive.presentation.di

import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.domain.di.DomainModule
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModelFactory
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModelFactory
import ipvc.tp.devhive.presentation.viewmodel.comment.CommentViewModelFactory
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModelFactory
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModelFactory
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModelFactory

/**
 * Módulo de injeção de dependência para a camada de apresentação
 */
object PresentationModule {

    // ViewModelFactories
    private fun provideAuthViewModelFactory(useCases: DomainModule.UseCases): AuthViewModelFactory {
        return AuthViewModelFactory(
            useCases.registerUser,
            useCases.loginUser,
            useCases.logoutUser
        )
    }

    private fun provideMaterialViewModelFactory(useCases: DomainModule.UseCases): MaterialViewModelFactory {
        return MaterialViewModelFactory(
            useCases.getMaterials,
            useCases.createMaterial,
            useCases.toggleBookmark
        )
    }

    private fun provideCommentViewModelFactory(useCases: DomainModule.UseCases): CommentViewModelFactory {
        return CommentViewModelFactory(
            useCases.createComment,
            useCases.likeComment
        )
    }

    private fun provideChatViewModelFactory(useCases: DomainModule.UseCases): ChatViewModelFactory {
        return ChatViewModelFactory(
            useCases.createChat,
            useCases.sendMessage
        )
    }

    private fun provideProfileViewModelFactory(useCases: DomainModule.UseCases): ProfileViewModelFactory {
        return ProfileViewModelFactory()
    }

    private fun provideStudyGroupViewModelFactory(useCases: DomainModule.UseCases): StudyGroupViewModelFactory {
        return StudyGroupViewModelFactory(
            useCases.createStudyGroup,
            useCases.joinStudyGroup,
            useCases.sendGroupMessage
        )
    }

    // Função para inicializar todas as factories de ViewModels
    fun provideViewModelFactories(useCases: DomainModule.UseCases): ViewModelFactories {
        return ViewModelFactories(
            authViewModelFactory = provideAuthViewModelFactory(useCases),
            materialViewModelFactory = provideMaterialViewModelFactory(useCases),
            commentViewModelFactory = provideCommentViewModelFactory(useCases),
            chatViewModelFactory = provideChatViewModelFactory(useCases),
            profileViewModelFactory = provideProfileViewModelFactory(useCases),
            studyGroupViewModelFactory = provideStudyGroupViewModelFactory(useCases)
        )
    }

    // Classe para agrupar todas as factories de ViewModels
    data class ViewModelFactories(
        val authViewModelFactory: ViewModelProvider.Factory,
        val materialViewModelFactory: ViewModelProvider.Factory,
        val commentViewModelFactory: ViewModelProvider.Factory,
        val chatViewModelFactory: ViewModelProvider.Factory,
        val profileViewModelFactory: ViewModelProvider.Factory,
        val studyGroupViewModelFactory: ViewModelProvider.Factory
    )
}
