package ipvc.tp.devhive.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import ipvc.tp.devhive.presentation.ui.intro.IntroActivity
import ipvc.tp.devhive.presentation.ui.main.MainActivity

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        val isAuthenticated = FirebaseAuth.getInstance().currentUser != null

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = when {
                isFirstRun -> {
                    // Primeira execução, mostra a introdução
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                        .edit {
                            putBoolean("isFirstRun", false)
                        }
                    Intent(this, IntroActivity::class.java)
                }
                !isAuthenticated -> {
                    Intent(this, LoginActivity::class.java)
                }
                else -> {
                    Intent(this, MainActivity::class.java)
                }
            }

            startActivity(intent)
            finish()
        }, 2000)
    }
}
