package ipvc.tp.devhive.presentation.ui.main.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import ipvc.tp.devhive.presentation.ui.intro.IntroActivity
import ipvc.tp.devhive.presentation.util.LocaleHelper
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var switchNotifications: Switch
    private lateinit var switchPrivateProfile: Switch
    private lateinit var itemLanguage: LinearLayout
    private lateinit var btnLogout: MaterialButton

    private val profileViewModel: ProfileViewModel by viewModels()

    private val languageChangeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        toolbar = findViewById(R.id.toolbar_settings)
        switchNotifications = findViewById(R.id.switch_notifications)
        switchPrivateProfile = findViewById(R.id.switch_private_profile)
        itemLanguage = findViewById(R.id.item_language_setting)
        btnLogout = findViewById(R.id.btn_logout)

        setupToolbar()
        setupListeners()
        loadCurrentSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadCurrentSettings() {
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)
        switchPrivateProfile.isChecked = prefs.getBoolean("private_profile_enabled", true)
    }

    private fun setupListeners() {
        itemLanguage.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            languageChangeLauncher.launch(intent)
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Notificações: $isChecked", Toast.LENGTH_SHORT).show()
            getSharedPreferences("settings_prefs", Context.MODE_PRIVATE).edit {
                putBoolean("notifications_enabled", isChecked)
            }
        }

        switchPrivateProfile.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Perfil Privado: $isChecked", Toast.LENGTH_SHORT).show()
            getSharedPreferences("settings_prefs", Context.MODE_PRIVATE).edit {
                putBoolean("private_profile_enabled", isChecked)
            }
        }

        btnLogout.setOnClickListener {
            profileViewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finishAffinity()
        }

    }

    fun onViewSlidesClick(view: View) {
        Toast.makeText(this, "Ver slides novamente clicado", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, IntroActivity::class.java)
        intent.putExtra("force_show_intro", true)
        startActivity(intent)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}