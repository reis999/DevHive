package ipvc.tp.devhive.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import ipvc.tp.devhive.presentation.ui.intro.IntroActivity
import ipvc.tp.devhive.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Verifica se é a primeira execução do app
        val isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        // Verifica se o usuário está autenticado
        val isAuthenticated = FirebaseAuth.getInstance().currentUser != null

        // Atraso para exibir a tela de splash
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = when {
                isFirstRun -> {
                    // Primeira execução, mostra a introdução
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                        .edit()
                        .putBoolean("isFirstRun", false)
                        .apply()
                    Intent(this, IntroActivity::class.java)
                }
                !isAuthenticated -> {
                    // Não está autenticado, mostra a tela de login
                    Intent(this, LoginActivity::class.java)
                }
                else -> {
                    // Já está autenticado, vai direto para a tela principal
                    Intent(this, MainActivity::class.java)
                }
            }

            startActivity(intent)
            finish()
        }, 2000) // 2 segundos
    }
}
