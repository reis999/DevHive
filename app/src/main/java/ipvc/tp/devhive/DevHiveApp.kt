package ipvc.tp.devhive

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import ipvc.tp.devhive.data.di.DataModule
import ipvc.tp.devhive.domain.di.DomainModule
import ipvc.tp.devhive.presentation.di.PresentationModule
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DevHiveApp : Application() {

    // Escopo da aplicação para operações em segundo plano
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

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)

        // Inicializa os componentes da aplicação
        initializeComponents()

        // Configura o tema (claro/escuro)
        setupTheme()

        // Inicia o monitoramento de sincronização
        startSyncMonitoring()
    }

    private fun initializeComponents() {
        // Inicializa os componentes da camada de dados
        dataComponents = DataModule.provideDataComponents(this)

        // Inicializa os casos de uso da camada de domínio
        useCases = DomainModule.provideUseCases(
            dataComponents.userRepository,
            dataComponents.materialRepository,
            dataComponents.commentRepository,
            dataComponents.chatRepository
        )

        // Inicializa as factories de ViewModels da camada de apresentação
        viewModels = PresentationModule.provideViewModelFactories(useCases)
    }

    private fun setupTheme() {
        // Por padrão, segue o tema do sistema
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun startSyncMonitoring() {
        // Inicia o monitoramento de sincronização em segundo plano
        dataComponents.syncManager.startMonitoring()

        // Realiza uma sincronização inicial
        applicationScope.launch {
            dataComponents.syncManager.syncPendingData()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Para o monitoramento de sincronização
        dataComponents.syncManager.stopMonitoring()
    }

    companion object {
        private lateinit var instance: DevHiveApp

        fun getDataComponents(): DataModule.DataComponents {
            return instance.dataComponents
        }

        fun getUseCases(): DomainModule.UseCases {
            return instance.useCases
        }

        fun getViewModelFactories(): PresentationModule.ViewModelFactories {
            return instance.viewModels
        }

        fun getAppContext(): Context {
            return instance.applicationContext
        }
    }
}
