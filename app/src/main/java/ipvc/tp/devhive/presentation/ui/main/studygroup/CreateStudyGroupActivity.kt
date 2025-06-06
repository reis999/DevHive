package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModel

class CreateStudyGroupActivity : AppCompatActivity() {

    private val studyGroupViewModel: StudyGroupViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var tilSubject: TextInputLayout
    private lateinit var actvSubject: AutoCompleteTextView
    private lateinit var etTags: EditText
    private lateinit var btnAddTag: Button
    private lateinit var chipGroupTags: ChipGroup
    private lateinit var btnCreate: Button
    private lateinit var progressBar: ProgressBar

    private val tags = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_study_group)

        // Inicializa as views
        toolbar = findViewById(R.id.toolbar)
        etName = findViewById(R.id.et_name)
        etDescription = findViewById(R.id.et_description)
        tilSubject = findViewById(R.id.til_subject)
        actvSubject = findViewById(R.id.actv_subject)
        etTags = findViewById(R.id.et_tags)
        btnAddTag = findViewById(R.id.btn_add_tag)
        chipGroupTags = findViewById(R.id.chip_group_tags)
        btnCreate = findViewById(R.id.btn_create)
        progressBar = findViewById(R.id.progress_bar)

        // Configura a toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.create_study_group)

        // Configura o adapter para as disciplinas
        val subjects = resources.getStringArray(R.array.subjects)
        val subjectAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, subjects)
        actvSubject.setAdapter(subjectAdapter)

        // Configura os listeners
        btnAddTag.setOnClickListener {
            addTag()
        }

        btnCreate.setOnClickListener {
            createStudyGroup()
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

    private fun createStudyGroup() {
        if (!validateFields()) {
            return
        }

        // Mostra o progresso
        progressBar.visibility = View.VISIBLE
        btnCreate.isEnabled = false

        // Obtém os valores dos campos
        val name = etName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val subject = actvSubject.text.toString().trim()

        // implementação real: usar studyGroupViewModel.createStudyGroup()
        simulateStudyGroupCreation(name, description, subject, tags)
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (etName.text.toString().trim().isEmpty()) {
            etName.error = getString(R.string.field_required)
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

        return isValid
    }

    private fun simulateStudyGroupCreation(
        name: String,
        description: String,
        subject: String,
        tags: List<String>
    ) {
        // Simula um atraso para a criação
        btnCreate.postDelayed({
            // Esconde o progresso
            progressBar.visibility = View.GONE
            btnCreate.isEnabled = true

            // Mostra mensagem de sucesso
            Toast.makeText(this, R.string.study_group_created, Toast.LENGTH_SHORT).show()

            // Fecha a atividade
            finish()
        }, 2000)
    }
}
