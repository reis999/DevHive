package ipvc.tp.devhive.presentation.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.util.showToast
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthEvent
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModel

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

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

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.email_required)
            return
        } else {
            tilEmail.error = null
        }

        showLoading(true)
        authViewModel.resetPassword()
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
