package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ActivityCreateStudyGroupBinding
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupGeneralResult
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModel


@AndroidEntryPoint
class CreateStudyGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateStudyGroupBinding
    private val viewModel: StudyGroupViewModel by viewModels()

    private val tags = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStudyGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.create_study_group)
    }

    private fun setupClickListeners() {
        binding.btnAddTag.setOnClickListener {
            addTag()
        }

        binding.btnCreate.setOnClickListener {
            createStudyGroup()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.btnCreate.isEnabled = !isLoading
            Log.d("CreateGroupActivity", "isLoading State: $isLoading")
        }

        viewModel.createGroupResultEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is StudyGroupGeneralResult.Success<*> -> {
                        Log.d("CreateGroupActivity", "CreateSuccess: Finishing activity.")
                        finish()
                    }

                    is StudyGroupGeneralResult.Failure -> {
                        Toast.makeText(
                            this,
                            "Erro ao criar grupo: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("CreateGroupActivity", "CreateFailure: ${result.message}")
                    }
                }
            }
        }

        viewModel.currentUser.observe(this) { user ->
            if (user == null) {
                Toast.makeText(this, R.string.user_not_auth, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addTag() {
        val tagName = binding.etTags.text.toString().trim()
        if (tagName.isNotEmpty() && !tags.any { it.equals(tagName, ignoreCase = true) }) {
            tags.add(tagName)
            addChipToGroup(tagName, binding.chipGroupTags)
            binding.etTags.text?.clear()
        } else if (tagName.isNotEmpty()){
            Toast.makeText(this, R.string.category_already_exists, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addChipToGroup(text: String, chipGroup: ChipGroup) {
        val chip = Chip(this).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                chipGroup.removeView(this)
                tags.remove(text)
            }
        }
        chipGroup.addView(chip)
    }

    private fun createStudyGroup() {
        if (!validateFields()) {
            return
        }

        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val isPrivate = binding.switchPrivate.isChecked

        viewModel.createStudyGroup(name, description, isPrivate, ArrayList(tags))
    }

    private fun validateFields(): Boolean {
        var isValid = true
        binding.etName.error = null
        binding.etDescription.error = null

        if (binding.etName.text.toString().trim().isEmpty()) {
            binding.etName.error = getString(R.string.field_required)
            isValid = false
        }

        if (binding.etDescription.text.toString().trim().isEmpty()) {
            binding.etDescription.error = getString(R.string.field_required)
            isValid = false
        }
        if (tags.isEmpty()){
            Toast.makeText(this, R.string.add_at_least_one_category, Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }
}