package ipvc.tp.devhive.presentation.ui.main

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ipvc.tp.devhive.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configura o NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configura a navegação de acordo com a orientação
        setupNavigation()
    }

    private fun setupNavigation() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Orientação retrato: usa BottomNavigationView
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigationView.setupWithNavController(navController)
        } else {
            // Orientação paisagem: usa NavigationRailView
            val navigationRailView = findViewById<NavigationRailView>(R.id.navigation_rail)
            navigationRailView.setupWithNavController(navController)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Recria a atividade para aplicar o layout correto
        recreate()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
