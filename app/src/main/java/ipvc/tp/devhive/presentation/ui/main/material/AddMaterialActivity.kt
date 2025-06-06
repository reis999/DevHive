package ipvc.tp.devhive.presentation.ui.main.material

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel

class AddMaterialActivity : AppCompatActivity() {

    private val materialViewModel: MaterialViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var ivCover: ImageView
    private lateinit var btnSelectCover: Button
    private lateinit var btnSelectFile: Button
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var tilSubject: TextInputLayout
    private lateinit var actvSubject: AutoCompleteTextView
    private lateinit var tilInstitution: TextInputLayout
    private lateinit var actvInstitution: AutoCompleteTextView
    private lateinit var tilCourse: TextInputLayout
    private lateinit var actvCourse: AutoCompleteTextView
    private lateinit var etTags: EditText
    private lateinit var btnAddTag: Button
    private lateinit var chipGroupTags: ChipGroup
    private lateinit var btnPublish: Button
    private lateinit var progressBar: ProgressBar

    private var selectedCoverUri: Uri? = null
    private var selectedFileUri: Uri? = null
    private val tags = mutableListOf<String>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private val selectCoverLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedCoverUri = uri
                displaySelectedCover(uri)
            }
        }
    }

    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                displaySelectedFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_material)

        // Inicializa as views
        toolbar = findViewById(R.id.toolbar)
        ivCover = findViewById(R.id.iv_cover)
        btnSelectCover = findViewById(R.id.btn_select_cover)
        btnSelectFile = findViewById(R.id.btn_select_file)
        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        tilSubject = findViewById(R.id.til_subject)
        actvSubject = findViewById(R.id.actv_subject)
        tilInstitution = findViewById(R.id.til_institution)
        actvInstitution = findViewById(R.id.actv_institution)
        tilCourse = findViewById(R.id.til_course)
        actvCourse = findViewById(R.id.actv_course)
        etTags = findViewById(R.id.et_tags)
        btnAddTag = findViewById(R.id.btn_add_tag)
        chipGroupTags = findViewById(R.id.chip_group_tags)
        btnPublish = findViewById(R.id.btn_publish)
        progressBar = findViewById(R.id.progress_bar)

        // Configura a toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_material)

        // Configura os listeners
        btnSelectCover.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        btnSelectFile.setOnClickListener {
            openFilePicker()
        }

        btnAddTag.setOnClickListener {
            addTag()
        }

        btnPublish.setOnClickListener {
            publishMaterial()
        }

        // Configura os adapters para os AutoCompleteTextViews
        setupAutoCompleteAdapters()
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

    private fun setupAutoCompleteAdapters() {
        // Configura o adapter para as disciplinas
        val subjects = resources.getStringArray(R.array.subjects)
        val subjectAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, subjects)
        actvSubject.setAdapter(subjectAdapter)

        // Configura o adapter para as instituições
        // implementação real: ir buscar a bd
        val institutions = arrayOf(
            "Instituto Politécnico de Viana do Castelo",
            "Universidade do Minho",
            "Universidade do Porto",
            "Universidade de Lisboa",
            "Instituto Politécnico do Porto"
        )
        val institutionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, institutions)
        actvInstitution.setAdapter(institutionAdapter)

        // Configura o adapter para os cursos
        // implementação real: ir buscar a bd
        val courses = arrayOf(
            "Engenharia Informática",
            "Ciência da Computação",
            "Sistemas de Informação",
            "Engenharia de Software",
            "Tecnologias da Informação"
        )
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courses)
        actvCourse.setAdapter(courseAdapter)
    }

    private fun checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissão não concedida, pede ao utilizador
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // Mostra uma explicação ao utilizador
                Toast.makeText(
                    this,
                    R.string.permission_rationale,
                    Toast.LENGTH_LONG
                ).show()
            }

            // Pede a permissão
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            // Permissão já concedida, abre a galeria
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectCoverLauncher.launch(intent)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val mimeTypes = arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        selectFileLauncher.launch(intent)
    }

    private fun displaySelectedCover(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.material_placeholder)
            .error(R.drawable.material_placeholder)
            .centerCrop()
            .into(ivCover)
    }

    private fun displaySelectedFile(uri: Uri) {
        // Obtem o nome do ficheiro
        val fileName = getFileName(uri)
        btnSelectFile.text = fileName ?: getString(R.string.select_file)
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        }
    }

    private fun addTag() {
        val tag = etTags.text.toString().trim()
        if (tag.isNotEmpty() && !tags.contains(tag)) {
            tags.add(tag)
            addChip(tag)
            etTags.text.clear()
        }
    }

    private fun addChip(text: String) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroupTags.removeView(chip)
            tags.remove(text)
        }
        chipGroupTags.addView(chip)
    }

    private fun publishMaterial() {
        // Valida os campos
        if (!validateFields()) {
            return
        }

        // Mostra o progresso
        progressBar.visibility = View.VISIBLE
        btnPublish.isEnabled = false

        // Obtém os valores dos campos
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val subject = actvSubject.text.toString().trim()
        val institution = actvInstitution.text.toString().trim()
        val course = actvCourse.text.toString().trim()

        // implementação real: usar materialViewModel.createMaterial()
        simulateUpload(title, description, subject, institution, course, tags)
    }

    // valida os campos
    private fun validateFields(): Boolean {
        var isValid = true

        if (etTitle.text.toString().trim().isEmpty()) {
            etTitle.error = getString(R.string.field_required)
            isValid = false
        }

        if (etDescription.text.toString().trim().isEmpty()) {
            etDescription.error = getString(R.string.field_required)
            isValid = false
        }

        if (actvSubject.text.toString().trim().isEmpty()) {
            tilSubject.error = getString(R.string.field_required)
            isValid = false
        } else {
            tilSubject.error = null
        }

        if (actvInstitution.text.toString().trim().isEmpty()) {
            tilInstitution.error = getString(R.string.field_required)
            isValid = false
        } else {
            tilInstitution.error = null
        }

        if (actvCourse.text.toString().trim().isEmpty()) {
            tilCourse.error = getString(R.string.field_required)
            isValid = false
        } else {
            tilCourse.error = null
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, R.string.select_file_required, Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun simulateUpload(
        title: String,
        description: String,
        subject: String,
        institution: String,
        course: String,
        tags: List<String>
    ) {
        // Simula um atraso para o upload
        btnPublish.postDelayed({
            // Esconde o progresso
            progressBar.visibility = View.GONE
            btnPublish.isEnabled = true

            // Mostra mensagem de sucesso
            Toast.makeText(this, R.string.material_published, Toast.LENGTH_SHORT).show()

            // Fecha a atividade
            finish()
        }, 2000)
    }
}
