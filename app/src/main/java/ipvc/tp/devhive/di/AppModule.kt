package ipvc.tp.devhive.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ipvc.tp.devhive.domain.repository.AuthRepository
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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Use Cases - Auth
    @Provides
    fun provideRegisterUserUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository
    ): RegisterUserUseCase {
        return RegisterUserUseCase(authRepository, userRepository)
    }

    @Provides
    fun provideLoginUserUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository
    ): LoginUserUseCase {
        return LoginUserUseCase(authRepository, userRepository)
    }

    @Provides
    fun provideLogoutUserUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository
    ): LogoutUserUseCase {
        return LogoutUserUseCase(authRepository ,userRepository)
    }

    // Use Cases - Material
    @Provides
    fun provideGetMaterialsUseCase(
        materialRepository: MaterialRepository
    ): GetMaterialsUseCase {
        return GetMaterialsUseCase(materialRepository)
    }

    @Provides
    fun provideCreateMaterialUseCase(
        materialRepository: MaterialRepository,
        userRepository: UserRepository
    ): CreateMaterialUseCase {
        return CreateMaterialUseCase(materialRepository, userRepository)
    }

    @Provides
    fun provideToggleBookmarkUseCase(
        materialRepository: MaterialRepository
    ): ToggleBookmarkUseCase {
        return ToggleBookmarkUseCase(materialRepository)
    }

    // Use Cases - Comment
    @Provides
    fun provideCreateCommentUseCase(
        commentRepository: CommentRepository,
        userRepository: UserRepository
    ): CreateCommentUseCase {
        return CreateCommentUseCase(commentRepository, userRepository)
    }

    @Provides
    fun provideLikeCommentUseCase(
        commentRepository: CommentRepository,
        userRepository: UserRepository
    ): LikeCommentUseCase {
        return LikeCommentUseCase(commentRepository, userRepository)
    }

    // Use Cases - Chat
    @Provides
    fun provideCreateChatUseCase(
        chatRepository: ChatRepository
    ): CreateChatUseCase {
        return CreateChatUseCase(chatRepository)
    }

    @Provides
    fun provideSendMessageUseCase(
        chatRepository: ChatRepository
    ): SendMessageUseCase {
        return SendMessageUseCase(chatRepository)
    }

    // Use Cases - Study Group
    @Provides
    fun provideCreateStudyGroupUseCase(
        studyGroupRepository: StudyGroupRepository,
        userRepository: UserRepository
    ): CreateStudyGroupUseCase {
        return CreateStudyGroupUseCase(studyGroupRepository, userRepository)
    }

    @Provides
    fun provideJoinStudyGroupUseCase(
        studyGroupRepository: StudyGroupRepository,
        userRepository: UserRepository
    ): JoinStudyGroupUseCase {
        return JoinStudyGroupUseCase(studyGroupRepository, userRepository)
    }

    @Provides
    fun provideSendGroupMessageUseCase(
        studyGroupRepository: StudyGroupRepository,
        userRepository: UserRepository
    ): SendGroupMessageUseCase {
        return SendGroupMessageUseCase(studyGroupRepository, userRepository)
    }

    // Use Cases - Sync

    @Provides
    fun provideSyncDataUseCase(
        userRepository: UserRepository,
        materialRepository: MaterialRepository,
        commentRepository: CommentRepository,
        chatRepository: ChatRepository,
        studyGroupRepository: StudyGroupRepository
    ): SyncDataUseCase {
        return SyncDataUseCase(userRepository, materialRepository, commentRepository, chatRepository, studyGroupRepository)
    }
}
