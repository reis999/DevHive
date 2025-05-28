package ipvc.tp.devhive.data.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import ipvc.tp.devhive.data.local.AppDatabase
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
import ipvc.tp.devhive.data.repository.ChatRepository
import ipvc.tp.devhive.data.repository.CommentRepository
import ipvc.tp.devhive.data.repository.MaterialRepository
import ipvc.tp.devhive.data.repository.StudyGroupRepository
import ipvc.tp.devhive.data.repository.UserRepository
import ipvc.tp.devhive.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Módulo de injeção de dependência para a camada de dados
 * Pode ser usado com Dagger/Hilt ou Koin
 */
object DataModule {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Firestore
    private fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // Base de Dados local
    private fun provideAppDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // DAOs
    private fun provideUserDao(db: AppDatabase) = db.userDao()
    private fun provideMaterialDao(db: AppDatabase) = db.materialDao()
    private fun provideCommentDao(db: AppDatabase) = db.commentDao()
    private fun provideChatDao(db: AppDatabase) = db.chatDao()
    private fun provideMessageDao(db: AppDatabase) = db.messageDao()
    private fun provideStudyGroupDao(db: AppDatabase) = db.studyGroupDao()
    private fun provideGroupMessageDao(db: AppDatabase) = db.groupMessageDao()

    // Serviços remotos
    private fun provideUserService(firestore: FirebaseFirestore) = UserService(firestore)
    private fun provideMaterialService(firestore: FirebaseFirestore) = MaterialService(firestore)
    private fun provideCommentService(firestore: FirebaseFirestore) = CommentService(firestore)
    private fun provideChatService(firestore: FirebaseFirestore) = ChatService(firestore)
    private fun provideStudyGroupService(firestore: FirebaseFirestore) = StudyGroupService(firestore)

    // Repositórios
    private fun provideUserRepository(
        userDao: UserDao,
        userService: UserService
    ) = UserRepository(userDao, userService, applicationScope)

    private fun provideMaterialRepository(
        materialDao: MaterialDao,
        materialService: MaterialService
    ) = MaterialRepository(materialDao, materialService, applicationScope)

    private fun provideCommentRepository(
        commentDao: CommentDao,
        commentService: CommentService
    ) = CommentRepository(commentDao, commentService, applicationScope)

    private fun provideChatRepository(
        chatDao: ChatDao,
        messageDao: MessageDao,
        chatService: ChatService
    ) = ChatRepository(chatDao, messageDao, chatService, applicationScope)

    private fun provideStudyGroupRepository(
        studyGroupDao: StudyGroupDao,
        groupMessageDao: GroupMessageDao,
        studyGroupService: StudyGroupService
    ) = StudyGroupRepository(studyGroupDao, groupMessageDao, studyGroupService, applicationScope)

    // SyncManager
    private fun provideSyncManager(
        context: Context,
        userRepository: UserRepository,
        materialRepository: MaterialRepository,
        commentRepository: CommentRepository,
        chatRepository: ChatRepository,
        studyGroupRepository: StudyGroupRepository
    ) = SyncManager(
        context,
        userRepository,
        materialRepository,
        commentRepository,
        chatRepository,
        studyGroupRepository,
        applicationScope
    )

    // Função para inicializar todos os componentes da camada de dados
    fun provideDataComponents(context: Context): DataComponents {
        val firestore = provideFirestore()
        val database = provideAppDatabase(context)

        val userDao = provideUserDao(database)
        val materialDao = provideMaterialDao(database)
        val commentDao = provideCommentDao(database)
        val chatDao = provideChatDao(database)
        val messageDao = provideMessageDao(database)
        val studyGroupDao = provideStudyGroupDao(database)
        val groupMessageDao = provideGroupMessageDao(database)

        val userService = provideUserService(firestore)
        val materialService = provideMaterialService(firestore)
        val commentService = provideCommentService(firestore)
        val chatService = provideChatService(firestore)
        val studyGroupService = provideStudyGroupService(firestore)

        val userRepository = provideUserRepository(userDao, userService)
        val materialRepository = provideMaterialRepository(materialDao, materialService)
        val commentRepository = provideCommentRepository(commentDao, commentService)
        val chatRepository = provideChatRepository(chatDao, messageDao, chatService)
        val studyGroupRepository = provideStudyGroupRepository(studyGroupDao, groupMessageDao, studyGroupService)

        val syncManager = provideSyncManager(
            context,
            userRepository,
            materialRepository,
            commentRepository,
            chatRepository,
            studyGroupRepository
        )

        return DataComponents(
            userRepository = userRepository,
            materialRepository = materialRepository,
            commentRepository = commentRepository,
            chatRepository = chatRepository,
            studyGroupRepository = studyGroupRepository,
            syncManager = syncManager
        )
    }

    // Classe para agrupar todos os componentes da camada de dados
    data class DataComponents(
        val userRepository: UserRepository,
        val materialRepository: MaterialRepository,
        val commentRepository: CommentRepository,
        val chatRepository: ChatRepository,
        val studyGroupRepository: StudyGroupRepository,
        val syncManager: SyncManager
    )
}
