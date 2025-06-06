package ipvc.tp.devhive

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import ipvc.tp.devhive.data.di.DataModule
import ipvc.tp.devhive.domain.di.DomainModule
import ipvc.tp.devhive.domain.repository.AuthRepository
import ipvc.tp.devhive.domain.repository.ChatRepository
import ipvc.tp.devhive.domain.repository.CommentRepository
import ipvc.tp.devhive.domain.repository.MaterialRepository
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import ipvc.tp.devhive.presentation.di.PresentationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class DevHiveApp : Application() {

    // Escopo da aplicação para operações em segundo plano
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Componentes da aplicação
    lateinit var authRepository: AuthRepository
    lateinit var userRepository: UserRepository
    lateinit var materialRepository: MaterialRepository
    lateinit var commentRepository: CommentRepository
    lateinit var chatRepository: ChatRepository
    lateinit var studyGroupRepository: StudyGroupRepository

    private lateinit var dataComponents: DataModule.DataComponents

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializa os componentes da camada de dados
        dataComponents = DataModule.provideDataComponents(this)

        // Inicializa o Firebase
        initializeFirebase()

        // Inicializa os componentes da aplicação
        initializeComponents()

        // Configura o tema (claro/escuro)
        setupTheme()

        // Inicia o monitoramento de sincronização
        startSyncMonitoring()

        // Atribui os repositórios
        userRepository = dataComponents.userRepository
        materialRepository = dataComponents.materialRepository
        commentRepository = dataComponents.commentRepository
        chatRepository = dataComponents.chatRepository
        studyGroupRepository = dataComponents.studyGroupRepository
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Em caso de erro na inicialização do Firebase, continua sem ele
            e.printStackTrace()
        }
    }

    private fun initializeComponents() {
        try {
            // Inicializa os casos de uso da camada de domínio
            useCases = DomainModule.provideUseCases(
                dataComponents.authRepository,
                dataComponents.userRepository,
                dataComponents.materialRepository,
                dataComponents.commentRepository,
                dataComponents.chatRepository,
                dataComponents.studyGroupRepository
            )

            // Inicializa as factories de ViewModels da camada de apresentação
            viewModels = PresentationModule.provideViewModelFactories(useCases)
        } catch (e: Exception) {
            // Em caso de erro, cria instâncias vazias para evitar crashes
            e.printStackTrace()
            // Em uma implementação real, você poderia criar implementações de fallback
        }
    }

    private fun setupTheme() {
        // Por padrão, segue o tema do sistema
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun startSyncMonitoring() {
        try {
            // Inicia o monitoramento de sincronização em segundo plano
            if (::dataComponents.isInitialized) {
                dataComponents.syncManager.startMonitoring()

                // Realiza uma sincronização inicial
                applicationScope.launch {
                    dataComponents.syncManager.syncPendingData()
                }
            }
        } catch (e: Exception) {
            // Em caso de erro, continua sem sincronização
            e.printStackTrace()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            // Para o monitoramento de sincronização
            if (::dataComponents.isInitialized) {
                dataComponents.syncManager.stopMonitoring()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private lateinit var instance: DevHiveApp
        private lateinit var useCases: DomainModule.UseCases
        private lateinit var viewModels: PresentationModule.ViewModelFactories

        fun getDataComponents(): DataModule.DataComponents {
            return if (::instance.isInitialized && instance::dataComponents.isInitialized) {
                instance.dataComponents
            } else {
                throw IllegalStateException("DataComponents not initialized")
            }
        }

        fun getUseCases(): DomainModule.UseCases {
            return if (::useCases.isInitialized) {
                useCases
            } else {
                throw IllegalStateException("UseCases not initialized")
            }
        }

        fun getViewModelFactories(): PresentationModule.ViewModelFactories {
            return if (::viewModels.isInitialized) {
                viewModels
            } else {
                throw IllegalStateException("ViewModelFactories not initialized")
            }
        }

        fun getAppContext(): Context {
            return if (::instance.isInitialized) {
                instance.applicationContext
            } else {
                throw IllegalStateException("Application not initialized")
            }
        }
    }
}
