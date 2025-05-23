package ipvc.tp.devhive.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import ipvc.tp.devhive.presentation.ui.intro.IntroActivity
import ipvc.tp.devhive.presentation.ui.main.MainActivity
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthState
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModel

class SplashActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inicializa o ViewModel
        val factory = DevHiveApp.getViewModelFactories().authViewModelFactory
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // Observa o estado de autenticação
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Authenticated -> navigateToMain()
                is AuthState.Unauthenticated -> checkFirstLaunch()
                else -> {} // Ignora outros estados
            }
        }

        // Atraso para exibir a tela de splash
        Handler(Looper.getMainLooper()).postDelayed({
            // Se o estado de autenticação ainda não foi definido, verifica o primeiro lançamento
            if (authViewModel.authState.value !is AuthState.Authenticated) {
                checkFirstLaunch()
            }
        }, SPLASH_DELAY)
    }

    private fun checkFirstLaunch() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            // Primeiro lançamento, mostra a introdução
            navigateToIntro()

            // Marca como não sendo mais o primeiro lançamento
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        } else {
            // Não é o primeiro lançamento, vai para o login
            navigateToLogin()
        }
    }

    private fun navigateToIntro() {
        val intent = Intent(this, IntroActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val SPLASH_DELAY = 2000L // 2 segundos
        private const val PREFS_NAME = "DevHivePrefs"
        private const val KEY_FIRST_LAUNCH = "isFirstLaunch"
    }
}
