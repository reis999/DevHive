package ipvc.tp.devhive.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.ui.main.MainActivity
import ipvc.tp.devhive.presentation.util.showSnackbar
import ipvc.tp.devhive.presentation.util.showToast
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthEvent
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthState
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModel

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: MaterialButton
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser != null) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        // Inicializa as views
        tilEmail = findViewById(R.id.til_email)
        etEmail = findViewById(R.id.et_email)
        tilPassword = findViewById(R.id.til_password)
        etPassword = findViewById(R.id.et_password)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        btnLogin = findViewById(R.id.btn_login)
        btnGoogleLogin = findViewById(R.id.btn_google_login)
        tvRegister = findViewById(R.id.tv_register)
        progressBar = findViewById(R.id.progress_bar)

        // Observa o estado de autenticação
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                is AuthState.Authenticated -> {
                    showLoading(false)
                    navigateToMain()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> showLoading(false)
            }
        }

        // Observa eventos de autenticação
        authViewModel.authEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { authEvent ->
                when (authEvent) {
                    is AuthEvent.LoginSuccess -> {
                        showToast(getString(R.string.login_success))
                        navigateToMain()
                    }
                    is AuthEvent.LoginFailure -> {
                        showError(authEvent.message)
                    }
                    else -> {} // Ignora outros eventos
                }
            }
        }


        btnLogin.setOnClickListener {
            login()
        }

        btnGoogleLogin.setOnClickListener {
            // Implementação do login com Google
            showToast(getString(R.string.google_login_not_implemented))
        }

        tvForgotPassword.setOnClickListener {
            // Implementação da recuperação de senha
            showForgotPasswordDialog()
        }

        tvRegister.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun login() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.email_required)
            isValid = false
        } else {
            tilEmail.error = null
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.password_required)
            isValid = false
        } else {
            tilPassword.error = null
        }

        if (isValid) {
            authViewModel.login(email, password)
        }
    }

    private fun showForgotPasswordDialog() {
        // Implementação do diálogo de recuperação de senha
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            btnGoogleLogin.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnLogin.isEnabled = true
            btnGoogleLogin.isEnabled = true
        }
    }

    private fun showError(message: String) {
        findViewById<View>(android.R.id.content).showSnackbar(message)
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
