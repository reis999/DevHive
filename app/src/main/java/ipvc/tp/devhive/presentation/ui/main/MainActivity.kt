package ipvc.tp.devhive.presentation.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ActivityMainBinding
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthState
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        authViewModel.authState.observe(this) { state ->
            if (state is AuthState.Unauthenticated || state is AuthState.Error) {
                if (FirebaseAuth.getInstance().currentUser == null && !isFinishing) {
                    navigateToLogin()
                }
            } else if (state is AuthState.Authenticated) {
                setContentView(binding.root)
                setupNavigation()
            }
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            navigateToLogin()
            return
        }

    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Verifica a orientação do dispositivo
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Configuração para modo paisagem (landscape)
            binding.navigationRail?.setupWithNavController(navController)
        } else {
            // Configuração para modo retrato (portrait)
            binding.bottomNavigation?.setupWithNavController(navController)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Recria a atividade para aplicar o layout correto
        recreate()
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser == null && !isFinishing) {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
