package ipvc.tp.devhive.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.ui.main.MainActivity
import ipvc.tp.devhive.presentation.util.showSnackbar
import ipvc.tp.devhive.presentation.util.showToast
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthEvent
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthState
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var ivBack: ImageView
    private lateinit var tilName: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var tilUsername: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilInstitution: TextInputLayout
    private lateinit var etInstitution: TextInputEditText
    private lateinit var tilCourse: TextInputLayout
    private lateinit var etCourse: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializa as views
        ivBack = findViewById(R.id.iv_back)
        tilName = findViewById(R.id.til_name)
        etName = findViewById(R.id.et_name)
        tilUsername = findViewById(R.id.til_username)
        etUsername = findViewById(R.id.et_username)
        tilEmail = findViewById(R.id.til_email)
        etEmail = findViewById(R.id.et_email)
        tilPassword = findViewById(R.id.til_password)
        etPassword = findViewById(R.id.et_password)
        tilInstitution = findViewById(R.id.til_institution)
        etInstitution = findViewById(R.id.et_institution)
        tilCourse = findViewById(R.id.til_course)
        etCourse = findViewById(R.id.et_course)
        btnRegister = findViewById(R.id.btn_register)
        tvLogin = findViewById(R.id.tv_login)
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
                    is AuthEvent.RegisterSuccess -> {
                        showToast(getString(R.string.register_success))
                        navigateToMain()
                    }
                    is AuthEvent.RegisterFailure -> {
                        showError(authEvent.message)
                    }
                    else -> {} // Ignora outros eventos
                }
            }
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Configura os listeners
        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnRegister.setOnClickListener {
            register()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun register() {
        // Valida os campos
        val name = etName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val institution = etInstitution.text.toString().trim()
        val course = etCourse.text.toString().trim()

        var isValid = true

        if (name.isEmpty()) {
            tilName.error = getString(R.string.name_required)
            isValid = false
        } else {
            tilName.error = null
        }

        if (username.isEmpty()) {
            tilUsername.error = getString(R.string.username_required)
            isValid = false
        } else {
            tilUsername.error = null
        }

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.email_required)
            isValid = false
        } else {
            tilEmail.error = null
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.password_required)
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = getString(R.string.password_too_short)
            isValid = false
        } else {
            tilPassword.error = null
        }

        if (institution.isEmpty()) {
            tilInstitution.error = getString(R.string.institution_required)
            isValid = false
        } else {
            tilInstitution.error = null
        }

        if (course.isEmpty()) {
            tilCourse.error = getString(R.string.course_required)
            isValid = false
        } else {
            tilCourse.error = null
        }

        if (isValid) {
            authViewModel.register(name, username, email, password, institution, course)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnRegister.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnRegister.isEnabled = true
        }
    }

    private fun showError(message: String) {
        findViewById<View>(android.R.id.content).showSnackbar(message)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
