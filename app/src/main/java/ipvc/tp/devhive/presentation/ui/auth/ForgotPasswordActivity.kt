package ipvc.tp.devhive.presentation.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.util.showSnackbar
import ipvc.tp.devhive.presentation.util.showToast
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthEvent
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    private lateinit var ivBack: ImageView
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnResetPassword: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Inicializa as views
        ivBack = findViewById(R.id.iv_back)
        tilEmail = findViewById(R.id.til_email)
        etEmail = findViewById(R.id.et_email)
        btnResetPassword = findViewById(R.id.btn_reset_password)
        progressBar = findViewById(R.id.progress_bar)

        // Inicializa o ViewModel
        val factory = DevHiveApp.getViewModelFactories().authViewModelFactory
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // Observa eventos de autenticação
        authViewModel.authEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { authEvent ->
                when (authEvent) {
                    is AuthEvent.PasswordResetSent -> {
                        showLoading(false)
                        showToast(getString(R.string.password_reset_sent))
                        finish()
                    }
                    else -> showLoading(false)
                }
            }
        }

        // Configura os listeners
        ivBack.setOnClickListener {
            onBackPressed()
        }

        btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        // Valida o campo de email
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.email_required)
            return
        } else {
            tilEmail.error = null
        }

        // Mostra o loading
        showLoading(true)

        // Chama o metodo de recuperação de senha
        authViewModel.resetPassword(email)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnResetPassword.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnResetPassword.isEnabled = true
        }
    }
}
