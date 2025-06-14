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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialGeneralResult
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel

@AndroidEntryPoint
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
    private lateinit var tilType: TextInputLayout
    private lateinit var actvType: AutoCompleteTextView
    private lateinit var etCategories: EditText
    private lateinit var btnAddCategory: Button
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var switchPublic: SwitchMaterial
    private lateinit var btnPublish: Button
    private lateinit var progressBar: ProgressBar

    private var selectedCoverUri: Uri? = null
    private var selectedFileUri: Uri? = null
    private val categories = mutableListOf<String>()

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

        initViews()
        setupToolbar()
        setupListeners()
        setupAutoCompleteAdapters()
        observeViewModel()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        ivCover = findViewById(R.id.iv_cover)
        btnSelectCover = findViewById(R.id.btn_select_cover)
        btnSelectFile = findViewById(R.id.btn_select_file)
        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        tilSubject = findViewById(R.id.til_subject)
        actvSubject = findViewById(R.id.actv_subject)
        tilType = findViewById(R.id.til_type)
        actvType = findViewById(R.id.actv_type)
        etCategories = findViewById(R.id.et_categories)
        btnAddCategory = findViewById(R.id.btn_add_category)
        chipGroupCategories = findViewById(R.id.chip_group_categories)
        switchPublic = findViewById(R.id.switch_public)
        btnPublish = findViewById(R.id.btn_publish)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_material)
    }

    private fun setupListeners() {
        btnSelectCover.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        btnSelectFile.setOnClickListener {
            openFilePicker()
        }

        btnAddCategory.setOnClickListener {
            addCategory()
        }

        btnPublish.setOnClickListener {
            publishMaterial()
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (materialViewModel.isLoading.value == true) {
                    Toast.makeText(this@AddMaterialActivity, "Upload em progresso...", Toast.LENGTH_SHORT).show()
                } else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun observeViewModel() {
        materialViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showProgress()
            } else {
                hideProgress()
            }
        }

        materialViewModel.createMaterialResultEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is MaterialGeneralResult.Success -> {
                        Toast.makeText(this, "Material criado com sucesso!", Toast.LENGTH_LONG).show()
                        finishActivity()
                    }
                    is MaterialGeneralResult.Failure -> {
                        showError(result.message)
                    }
                }
            }
        }
    }

    private fun finishActivity() {
        val resultIntent = Intent().apply {
            putExtra("MATERIAL_CREATED", true)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupAutoCompleteAdapters() {
        val subjects = resources.getStringArray(R.array.subjects)
        val subjectAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, subjects)
        actvSubject.setAdapter(subjectAdapter)

        val types = arrayOf(
            getString(R.string.type_pdf),
            getString(R.string.type_video),
            getString(R.string.type_audio),
            getString(R.string.type_image),
            getString(R.string.type_document),
            getString(R.string.type_presentation),
            getString(R.string.type_spreadsheet),
            getString(R.string.type_code),
            getString(R.string.type_other)
        )
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        actvType.setAdapter(typeAdapter)
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
        selectCoverLauncher.launch(intent)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val mimeTypes = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "image/*",
            "video/*",
            "audio/*"
        )
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

        btnSelectCover.text = getString(R.string.thumbnail_selected)
    }

    private fun displaySelectedFile(uri: Uri) {
        val fileName = getFileName(uri)
        btnSelectFile.text = if (fileName != null) {
            getString(R.string.file_selected, fileName)
        } else {
            getString(R.string.file_selected_generic)
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) {
                it.getString(nameIndex)
            } else null
        }
    }

    private fun addCategory() {
        val category = etCategories.text.toString().trim()
        if (category.isNotEmpty() && !categories.contains(category)) {
            categories.add(category)
            addChip(category)
            etCategories.text.clear()
        } else if (category.isNotEmpty()) {
            Toast.makeText(this, "Categoria já adicionada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addChip(text: String) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroupCategories.removeView(chip)
            categories.remove(text)
        }
        chipGroupCategories.addView(chip)
    }

    private fun publishMaterial() {
        if (!validateFields()) {
            return
        }

        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val subject = actvSubject.text.toString().trim()
        val type = actvType.text.toString().trim()
        val isPublic = switchPublic.isChecked

        materialViewModel.createMaterial(
            title = title,
            description = description,
            type = type,
            subject = subject,
            categories = categories,
            isPublic = isPublic,
            fileUri = selectedFileUri!!,
            thumbnailUri = selectedCoverUri
        )
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (etTitle.text.toString().trim().isEmpty()) {
            etTitle.error = getString(R.string.field_required)
            isValid = false
        } else {
            etTitle.error = null
        }

        if (etDescription.text.toString().trim().isEmpty()) {
            etDescription.error = getString(R.string.field_required)
            isValid = false
        } else {
            etDescription.error = null
        }

        if (actvSubject.text.toString().trim().isEmpty()) {
            tilSubject.error = getString(R.string.field_required)
            isValid = false
        } else {
            tilSubject.error = null
        }

        if (actvType.text.toString().trim().isEmpty()) {
            tilType.error = getString(R.string.field_required)
            isValid = false
        } else {
            tilType.error = null
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, R.string.select_file_required, Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun showProgress() {
        progressBar.visibility = View.VISIBLE
        btnPublish.isEnabled = false
        btnSelectFile.isEnabled = false
        btnSelectCover.isEnabled = false
        btnAddCategory.isEnabled = false

        etTitle.isEnabled = false
        etDescription.isEnabled = false
        actvSubject.isEnabled = false
        actvType.isEnabled = false
        etCategories.isEnabled = false
        switchPublic.isEnabled = false
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
        btnPublish.isEnabled = true
        btnSelectFile.isEnabled = true
        btnSelectCover.isEnabled = true
        btnAddCategory.isEnabled = true

        etTitle.isEnabled = true
        etDescription.isEnabled = true
        actvSubject.isEnabled = true
        actvType.isEnabled = true
        etCategories.isEnabled = true
        switchPublic.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Erro: $message", Toast.LENGTH_LONG).show()
    }
}