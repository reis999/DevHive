package ipvc.tp.devhive.domain.di

import ipvc.tp.devhive.domain.repository.ChatRepository
import ipvc.tp.devhive.domain.repository.CommentRepository
import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import ipvc.tp.devhive.domain.usecase.auth.LoginUserUseCase
import ipvc.tp.devhive.domain.usecase.auth.LogoutUserUseCase
import ipvc.tp.devhive.domain.usecase.auth.RegisterUserUseCase
import ipvc.tp.devhive.domain.usecase.chat.CreateChatUseCase
import ipvc.tp.devhive.domain.usecase.chat.SendMessageUseCase
import ipvc.tp.devhive.domain.usecase.comment.CreateCommentUseCase
import ipvc.tp.devhive.domain.usecase.comment.LikeCommentUseCase
import ipvc.tp.devhive.domain.usecase.material.CreateMaterialUseCase
import ipvc.tp.devhive.domain.usecase.material.GetMaterialsUseCase
import ipvc.tp.devhive.domain.usecase.material.ToggleBookmarkUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.CreateStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.JoinStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.SendGroupMessageUseCase
import ipvc.tp.devhive.domain.usecase.sync.SyncDataUseCase

/**
 * Módulo de injeção de dependência para a camada de domínio
 * Pode ser usado com Dagger/Hilt ou Koin
 */
object DomainModule {

    // Casos de uso de autenticação
    private fun provideRegisterUserUseCase(userRepository: UserRepository): RegisterUserUseCase {
        return RegisterUserUseCase(userRepository)
    }

    private fun provideLoginUserUseCase(userRepository: UserRepository): LoginUserUseCase {
        return LoginUserUseCase(userRepository)
    }

    private fun provideLogoutUserUseCase(userRepository: UserRepository): LogoutUserUseCase {
        return LogoutUserUseCase(userRepository)
    }

    // Casos de uso de materiais
    private fun provideGetMaterialsUseCase(materialRepository: MaterialRepository): GetMaterialsUseCase {
        return GetMaterialsUseCase(materialRepository)
    }

    private fun provideCreateMaterialUseCase(
        materialRepository: MaterialRepository,
        userRepository: UserRepository
    ): CreateMaterialUseCase {
        return CreateMaterialUseCase(materialRepository, userRepository)
    }

    private fun provideToggleBookmarkUseCase(materialRepository: MaterialRepository): ToggleBookmarkUseCase {
        return ToggleBookmarkUseCase(materialRepository)
    }

    // Casos de uso de comentários
    private fun provideCreateCommentUseCase(
        commentRepository: CommentRepository,
        userRepository: UserRepository
    ): CreateCommentUseCase {
        return CreateCommentUseCase(commentRepository, userRepository)
    }

    private fun provideLikeCommentUseCase(
        commentRepository: CommentRepository,
        userRepository: UserRepository
    ): LikeCommentUseCase {
        return LikeCommentUseCase(commentRepository, userRepository)
    }

    // Casos de uso de chat
    private fun provideCreateChatUseCase(chatRepository: ChatRepository): CreateChatUseCase {
        return CreateChatUseCase(chatRepository)
    }

    private fun provideSendMessageUseCase(chatRepository: ChatRepository): SendMessageUseCase {
        return SendMessageUseCase(chatRepository)
    }

    // Casos de uso de grupos de estudo
    private fun provideCreateStudyGroupUseCase(
        studyGroupRepository: StudyGroupRepository,
        userRepository: UserRepository
    ): CreateStudyGroupUseCase {
        return CreateStudyGroupUseCase(studyGroupRepository, userRepository)
    }

    private fun provideJoinStudyGroupUseCase(
        studyGroupRepository: StudyGroupRepository,
        userRepository: UserRepository
    ): JoinStudyGroupUseCase {
        return JoinStudyGroupUseCase(studyGroupRepository, userRepository)
    }

    private fun provideSendGroupMessageUseCase(
        studyGroupRepository: StudyGroupRepository,
        userRepository: UserRepository
    ): SendGroupMessageUseCase {
        return SendGroupMessageUseCase(studyGroupRepository, userRepository)
    }

    // Casos de uso de sincronização
    private fun provideSyncDataUseCase(
        userRepository: UserRepository,
        materialRepository: MaterialRepository,
        commentRepository: CommentRepository,
        chatRepository: ChatRepository,
        studyGroupRepository: StudyGroupRepository
    ): SyncDataUseCase {
        return SyncDataUseCase(userRepository, materialRepository, commentRepository, chatRepository, studyGroupRepository)
    }

    // Função para inicializar todos os casos de uso
    fun provideUseCases(
        userRepository: UserRepository,
        materialRepository: MaterialRepository,
        commentRepository: CommentRepository,
        chatRepository: ChatRepository,
        studyGroupRepository: StudyGroupRepository
    ): UseCases {
        return UseCases(
            registerUser = provideRegisterUserUseCase(userRepository),
            loginUser = provideLoginUserUseCase(userRepository),
            logoutUser = provideLogoutUserUseCase(userRepository),
            getMaterials = provideGetMaterialsUseCase(materialRepository),
            createMaterial = provideCreateMaterialUseCase(materialRepository, userRepository),
            toggleBookmark = provideToggleBookmarkUseCase(materialRepository),
            createComment = provideCreateCommentUseCase(commentRepository, userRepository),
            likeComment = provideLikeCommentUseCase(commentRepository, userRepository),
            createChat = provideCreateChatUseCase(chatRepository),
            sendMessage = provideSendMessageUseCase(chatRepository),
            createStudyGroup = provideCreateStudyGroupUseCase(studyGroupRepository, userRepository),
            joinStudyGroup = provideJoinStudyGroupUseCase(studyGroupRepository, userRepository),
            sendGroupMessage = provideSendGroupMessageUseCase(studyGroupRepository, userRepository),
            syncData = provideSyncDataUseCase(userRepository, materialRepository, commentRepository, chatRepository, studyGroupRepository)
        )
    }

    // Classe para agrupar todos os casos de uso
    data class UseCases(
        val registerUser: RegisterUserUseCase,
        val loginUser: LoginUserUseCase,
        val logoutUser: LogoutUserUseCase,
        val getMaterials: GetMaterialsUseCase,
        val createMaterial: CreateMaterialUseCase,
        val toggleBookmark: ToggleBookmarkUseCase,
        val createComment: CreateCommentUseCase,
        val likeComment: LikeCommentUseCase,
        val createChat: CreateChatUseCase,
        val sendMessage: SendMessageUseCase,
        val createStudyGroup: CreateStudyGroupUseCase,
        val joinStudyGroup: JoinStudyGroupUseCase,
        val sendGroupMessage: SendGroupMessageUseCase,
        val syncData: SyncDataUseCase
    )
}
