package ipvc.tp.devhive

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import ipvc.tp.devhive.data.di.DataModule
import ipvc.tp.devhive.domain.di.DomainModule
import ipvc.tp.devhive.presentation.di.PresentationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DevHiveApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Componentes da camada de dados
    private lateinit var dataComponents: DataModule.DataComponents

    // Componentes da camada de domínio
    private lateinit var useCases: DomainModule.UseCases

    // Componentes da camada de apresentação
    private lateinit var viewModels: PresentationModule.ViewModelFactories

    override fun onCreate() {
        super.onCreate()
        instance = this

        initializeFirebase()

        initializeComponents()

        setupTheme()

        startSyncMonitoring()
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
            // Inicializa os componentes da camada de dados
            dataComponents = DataModule.provideDataComponents(this)

            // Inicializa os casos de uso da camada de domínio
            useCases = DomainModule.provideUseCases(
                dataComponents.userRepository,
                dataComponents.materialRepository,
                dataComponents.commentRepository,
                dataComponents.chatRepository,
                dataComponents.studyGroupRepository,
            )

            // Inicializa as factories de ViewModels da camada de apresentação
            viewModels = PresentationModule.provideViewModelFactories(useCases)
        } catch (e: Exception) {
            // Em caso de erro, cria instâncias vazias para evitar crashes
            e.printStackTrace()
            // implementação real: criar implementações de fallback
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

        fun getDataComponents(): DataModule.DataComponents {
            return if (::instance.isInitialized && instance::dataComponents.isInitialized) {
                instance.dataComponents
            } else {
                throw IllegalStateException("DataComponents not initialized")
            }
        }

        fun getUseCases(): DomainModule.UseCases {
            return if (::instance.isInitialized && instance::useCases.isInitialized) {
                instance.useCases
            } else {
                throw IllegalStateException("UseCases not initialized")
            }
        }

        fun getViewModelFactories(): PresentationModule.ViewModelFactories {
            return if (::instance.isInitialized && instance::viewModels.isInitialized) {
                instance.viewModels
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
