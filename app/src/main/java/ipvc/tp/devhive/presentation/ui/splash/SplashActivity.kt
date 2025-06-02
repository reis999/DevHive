package ipvc.tp.devhive.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
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
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        try {
            val factory = DevHiveApp.getViewModelFactories().authViewModelFactory
            authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

            authViewModel.authState.observe(this) { state ->
                if (!hasNavigated) {
                    when (state) {
                        is AuthState.Authenticated -> navigateToMain()
                        is AuthState.Unauthenticated -> checkFirstLaunch()
                        is AuthState.Loading -> {
                        }
                        is AuthState.Error -> {
                            checkFirstLaunch()
                        }
                    }
                }
            }

            authViewModel.checkAuthState()

            Handler(Looper.getMainLooper()).postDelayed({
                if (!hasNavigated) {
                    checkFirstLaunch()
                }
            }, SPLASH_TIMEOUT)

        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).postDelayed({
                if (!hasNavigated) {
                    checkFirstLaunch()
                }
            }, SPLASH_DELAY)
        }
    }

    private fun checkFirstLaunch() {
        if (hasNavigated) return

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            navigateToIntro()
            prefs.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToIntro() {
        if (hasNavigated) return
        hasNavigated = true

        val intent = Intent(this, IntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        if (hasNavigated) return
        hasNavigated = true

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        if (hasNavigated) return
        hasNavigated = true

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val SPLASH_DELAY = 2000L
        private const val SPLASH_TIMEOUT = 3000L
        private const val PREFS_NAME = "DevHivePrefs"
        private const val KEY_FIRST_LAUNCH = "isFirstLaunch"
    }
}