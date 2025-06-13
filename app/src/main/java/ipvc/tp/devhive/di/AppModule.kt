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
import ipvc.tp.devhive.domain.usecase.studygroup.CheckUserIsAdminUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.CreateStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.DeleteStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.GetPublicStudyGroupsUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.GetStudyGroupByIdUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.GetStudyGroupMessagesUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.GetStudyGroupsByUserUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.GetStudyGroupsUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.JoinStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.LeaveStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.RemoveMemberUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.SendGroupMessageUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.UpdateStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.sync.SyncDataUseCase
import ipvc.tp.devhive.domain.usecase.user.GetCurrentUserUseCase
import ipvc.tp.devhive.domain.usecase.user.GetUserByIdUseCase
import ipvc.tp.devhive.domain.usecase.user.GetUsersByIdsUseCase
import ipvc.tp.devhive.domain.usecase.user.UpdateUserUseCase

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
    fun provideGetStudyGroupsUseCase(
        studyGroupRepository: StudyGroupRepository
    ): GetStudyGroupsUseCase {
        return GetStudyGroupsUseCase(studyGroupRepository)
    }

    @Provides
    fun provideGetStudyGroupByUserUseCase(
        studyGroupRepository: StudyGroupRepository
    ): GetStudyGroupsByUserUseCase {
        return GetStudyGroupsByUserUseCase(studyGroupRepository)
    }

    @Provides
    fun provideGetAllPublicStudyGroupsUseCase(
        studyGroupRepository: StudyGroupRepository
    ): GetPublicStudyGroupsUseCase {
        return GetPublicStudyGroupsUseCase(studyGroupRepository)
    }

    @Provides
    fun provideGetStudyGroupByIdUseCase(
        studyGroupRepository: StudyGroupRepository
    ): GetStudyGroupByIdUseCase {
        return GetStudyGroupByIdUseCase(studyGroupRepository)
    }

    @Provides
    fun provideGetGroupMessagesUseCase(
        studyGroupRepository: StudyGroupRepository
    ): GetStudyGroupMessagesUseCase {
        return GetStudyGroupMessagesUseCase(studyGroupRepository)
    }

    @Provides
    fun provideCheckUserIsAdminUseCase(
        studyGroupRepository: StudyGroupRepository
    ): CheckUserIsAdminUseCase {
        return CheckUserIsAdminUseCase(studyGroupRepository)
    }

    @Provides
    fun provideDeleteStudyGroupUseCase(
        studyGroupRepository: StudyGroupRepository
    ): DeleteStudyGroupUseCase {
        return DeleteStudyGroupUseCase(studyGroupRepository)
    }

    @Provides
    fun provideUpdateStudyGroupUseCase(
        studyGroupRepository: StudyGroupRepository
    ): UpdateStudyGroupUseCase {
        return UpdateStudyGroupUseCase(studyGroupRepository)
    }

    @Provides
    fun provideRemoveMemberUseCase(
        studyGroupRepository: StudyGroupRepository
    ): RemoveMemberUseCase {
        return RemoveMemberUseCase(studyGroupRepository)
    }

    @Provides
    fun provideLeaveStudyGroupUseCase(
        studyGroupRepository: StudyGroupRepository
    ): LeaveStudyGroupUseCase {
        return LeaveStudyGroupUseCase(studyGroupRepository)
    }

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

    // Use Cases - User
    @Provides
    fun provideGetCurrentUserUseCase(
        userRepository: UserRepository
    ): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(userRepository)
    }

    @Provides
    fun provideUpdateUserUseCase(
        userRepository: UserRepository
    ): UpdateUserUseCase {
        return UpdateUserUseCase(userRepository)
    }

    @Provides
    fun provideGetUserByIdUseCase(
        userRepository: UserRepository
    ): GetUserByIdUseCase {
        return GetUserByIdUseCase(userRepository)
    }

    @Provides
    fun provideGetUsersByIdsUseCase(
        userRepository: UserRepository
    ): GetUsersByIdsUseCase {
        return GetUsersByIdsUseCase(userRepository)
    }
}
