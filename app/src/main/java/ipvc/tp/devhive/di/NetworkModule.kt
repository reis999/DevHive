package ipvc.tp.devhive.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ipvc.tp.devhive.data.remote.service.ChatService
import ipvc.tp.devhive.data.remote.service.CommentService
import ipvc.tp.devhive.data.remote.service.MaterialService
import ipvc.tp.devhive.data.remote.service.StudyGroupService
import ipvc.tp.devhive.data.remote.service.UserService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideUserService(firestore: FirebaseFirestore): UserService {
        return UserService(firestore)
    }

    @Provides
    @Singleton
    fun provideMaterialService(firestore: FirebaseFirestore): MaterialService {
        return MaterialService(firestore)
    }

    @Provides
    @Singleton
    fun provideCommentService(firestore: FirebaseFirestore): CommentService {
        return CommentService(firestore)
    }

    @Provides
    @Singleton
    fun provideChatService(firestore: FirebaseFirestore): ChatService {
        return ChatService(firestore)
    }

    @Provides
    @Singleton
    fun provideStudyGroupService(firestore: FirebaseFirestore): StudyGroupService {
        return StudyGroupService(firestore)
    }



}