package ipvc.tp.devhive.presentation.ui.main

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
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
}
