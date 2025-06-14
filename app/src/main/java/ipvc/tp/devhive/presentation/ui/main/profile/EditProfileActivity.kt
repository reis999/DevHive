package ipvc.tp.devhive.presentation.ui.main.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileEvent
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()

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

        etEmail.apply {
            isFocusable = false
            isClickable = false
            isLongClickable = true
            inputType = InputType.TYPE_NULL
            setTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        etUsername.apply {
            isFocusable = false
            isClickable = false
            isLongClickable = true
            inputType = InputType.TYPE_NULL
            setTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        // Configura a toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.edit_profile)

        // Carrega os dados do perfil
        loadProfileData()

        // Configura os listeners
        btnChangePhoto.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        viewModel.userProfile.observe(this) { user ->
            user?.let { displayUserData(it) }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSave.isEnabled = !isLoading
        }

        viewModel.profileEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { profileEvent ->
                when (profileEvent) {
                    is ProfileEvent.ProfileUpdated -> {
                        Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    is ProfileEvent.Error -> {
                        Toast.makeText(this, profileEvent.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadProfileData() {
        viewModel.loadUserProfile()
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
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, R.string.permission_rationale, Toast.LENGTH_LONG).show()
            }
            requestPermissionLauncher.launch(permission)
        } else {
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
        val bio = etBio.text.toString().trim()
        val institution = etInstitution.text.toString().trim()
        val course = etCourse.text.toString().trim()

        viewModel.updateProfile(name, bio, institution, course, selectedImageUri)
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (etName.text.toString().trim().isEmpty()) {
            etName.error = getString(R.string.field_required)
            isValid = false
        }

        return isValid
    }
}