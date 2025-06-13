package ipvc.tp.devhive.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ipvc.tp.devhive.data.local.dao.ChatDao
import ipvc.tp.devhive.data.local.dao.CommentDao
import ipvc.tp.devhive.data.local.dao.GroupMessageDao
import ipvc.tp.devhive.data.local.dao.MaterialDao
import ipvc.tp.devhive.data.local.dao.MessageDao
import ipvc.tp.devhive.data.local.dao.StudyGroupDao
import ipvc.tp.devhive.data.local.dao.UserDao
import ipvc.tp.devhive.data.remote.service.ChatService
import ipvc.tp.devhive.data.remote.service.CommentService
import ipvc.tp.devhive.data.remote.service.MaterialService
import ipvc.tp.devhive.data.remote.service.StudyGroupService
import ipvc.tp.devhive.data.remote.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton
import ipvc.tp.devhive.data.repository.AuthRepository as DataAuthRepository
import ipvc.tp.devhive.data.repository.ChatRepository as DataChatRepository
import ipvc.tp.devhive.data.repository.CommentRepository as DataCommentRepository
import ipvc.tp.devhive.data.repository.MaterialRepository as DataMaterialRepository
import ipvc.tp.devhive.data.repository.StudyGroupRepository as DataStudyGroupRepository
import ipvc.tp.devhive.data.repository.UserRepository as DataUserRepository
import ipvc.tp.devhive.domain.repository.AuthRepository as DomainAuthRepository
import ipvc.tp.devhive.domain.repository.ChatRepository as DomainChatRepository
import ipvc.tp.devhive.domain.repository.CommentRepository as DomainCommentRepository
import ipvc.tp.devhive.domain.repository.MaterialRepository as DomainMaterialRepository
import ipvc.tp.devhive.domain.repository.StudyGroupRepository as DomainStudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository as DomainUserRepository


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): DomainAuthRepository {
        return DataAuthRepository(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        messageDao: MessageDao,
        chatService: ChatService,
        @ApplicationScope scope: CoroutineScope
    ): DomainChatRepository {
        return DataChatRepository(chatDao, messageDao, chatService, scope)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(
        commentDao: CommentDao,
        commentService: CommentService,
        @ApplicationScope scope: CoroutineScope
    ): DomainCommentRepository {
        return DataCommentRepository(commentDao, commentService, scope)
    }

    @Provides
    @Singleton
    fun provideMaterialRepository(
        materialDao: MaterialDao,
        materialService: MaterialService,
        @ApplicationScope scope: CoroutineScope
    ): DomainMaterialRepository {
        return DataMaterialRepository(materialDao, materialService, scope)
    }

    @Provides
    @Singleton
    fun provideStudyGroupRepository(
        studyGroupDao: StudyGroupDao,
        groupMessageDao: GroupMessageDao,
        studyGroupService: StudyGroupService,
        @ApplicationScope scope: CoroutineScope,
        firebaseStorage: FirebaseStorage
    ): DomainStudyGroupRepository {
        return DataStudyGroupRepository(studyGroupDao, groupMessageDao, studyGroupService, scope, firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        userService: UserService,
        @ApplicationScope scope: CoroutineScope
    ): DomainUserRepository {
        return DataUserRepository(userDao, userService, scope)
    }

}