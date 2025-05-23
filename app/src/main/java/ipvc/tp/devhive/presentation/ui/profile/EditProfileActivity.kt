package ipvc.tp.devhive.presentation.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var ivProfile: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var etName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etBio: EditText
    private lateinit var etInstitution: EditText
    private lateinit var etCourse: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Inicializa as views
        toolbar = findViewById(R.id.toolbar)
        ivProfile = findViewById(R.id.iv_profile)
        btnChangePhoto = findViewById(R.id.btn_change_photo)
        etName = findViewById(R.id.et_name)
        etUsername = findViewById(R.id.et_username)
        etEmail = findViewById(R.id.et_email)
        etBio = findViewById(R.id.et_bio)
        etInstitution = findViewById(R.id.et_institution)
        etCourse = findViewById(R.id.et_course)
        btnSave = findViewById(R.id.btn_save)
        progressBar = findViewById(R.id.progress_bar)

        // Configura a toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.edit_profile)

        // Inicializa o ViewModel
        val factory = DevHiveApp.getViewModelFactories().profileViewModelFactory
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        // Carrega os dados do perfil
        loadProfileData()

        // Configura os listeners
        btnChangePhoto.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadProfileData() {
        // Em uma implementação real, usaríamos profileViewModel.getCurrentUser()
        // Para fins de demonstração, usamos dados simulados
        val mockUser = getMockUser()
        displayUserData(mockUser)
    }

    private fun displayUserData(user: User) {
        etName.setText(user.name)
        etUsername.setText(user.username)
        etEmail.setText(user.email)
        etBio.setText(user.bio)
        etInstitution.setText(user.institution)
        etCourse.setText(user.course)

        // Carrega a imagem de perfil
        if (user.profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(ivProfile)
        }
    }

    private fun checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissão não concedida, solicita ao usuário
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // Mostra uma explicação ao usuário
                Toast.makeText(
                    this,
                    R.string.permission_rationale,
                    Toast.LENGTH_LONG
                ).show()
            }

            // Solicita a permissão
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            // Permissão já concedida, abre a galeria
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun displaySelectedImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.profile_placeholder)
            .error(R.drawable.profile_placeholder)
            .circleCrop()
            .into(ivProfile)
    }

    private fun saveProfile() {
        // Valida os campos
        if (!validateFields()) {
            return
        }

        // Mostra o progresso
        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        // Obtém os valores dos campos
        val name = etName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val bio = etBio.text.toString().trim()
        val institution = etInstitution.text.toString().trim()
        val course = etCourse.text.toString().trim()

        // Em uma implementação real, usaríamos profileViewModel.updateProfile()
        // Para fins de demonstração, simulamos a atualização
        simulateProfileUpdate(name, username, email, bio, institution, course)
    }

    private fun validateFields(): Boolean {
        var isValid = true

        // Valida o nome
        if (etName.text.toString().trim().isEmpty()) {
            etName.error = getString(R.string.field_required)
            isValid = false
        }

        // Valida o username
        if (etUsername.text.toString().trim().isEmpty()) {
            etUsername.error = getString(R.string.field_required)
            isValid = false
        }

        // Valida o email
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = getString(R.string.field_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.invalid_email)
            isValid = false
        }

        return isValid
    }

    private fun simulateProfileUpdate(
        name: String,
        username: String,
        email: String,
        bio: String,
        institution: String,
        course: String
    ) {
        // Simula um atraso para a atualização
        btnSave.postDelayed({
            // Esconde o progresso
            progressBar.visibility = View.GONE
            btnSave.isEnabled = true

            // Mostra mensagem de sucesso
            Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show()

            // Fecha a atividade
            finish()
        }, 2000)
    }

    private fun getMockUser(): User {
        // Simulamos um usuário para fins de demonstração
        return User(
            id = "user123",
            name = "David Reis",
            username = "davidreis",
            email = "david.reis@example.com",
            profileImageUrl = "",
            bio = "Estudante de Engenharia Informática no IPVC. Interessado em desenvolvimento mobile e inteligência artificial.",
            institution = "Instituto Politécnico de Viana do Castelo",
            course = "Licenciatura em Engenharia Informática",
            createdAt = java.util.Date(),
            lastLogin = java.util.Date(),
            isOnline = true,
            contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                materials = 12,
                comments = 45,
                likes = 78,
                sessions = 5
            )
        )
    }
}
