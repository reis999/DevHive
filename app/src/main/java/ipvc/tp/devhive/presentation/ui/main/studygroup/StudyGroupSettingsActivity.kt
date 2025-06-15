package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ActivityStudyGroupSettingsBinding
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupEvent
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupGeneralResult
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModel

@AndroidEntryPoint
class StudyGroupSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyGroupSettingsBinding
    private val viewModel: StudyGroupViewModel by viewModels()

    private var groupId: String? = null
    private var selectedImageUri: Uri? = null
    private val currentCategories = mutableListOf<String>()

    companion object {
        const val EXTRA_GROUP_ID_SETTINGS = "extra_group_id_settings"
    }

    private val manageMembersLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            groupId?.let { viewModel.loadStudyGroupDetails(it) }
            Toast.makeText(this, R.string.member_list_updated, Toast.LENGTH_SHORT).show() // Adicionar string
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.placeholder_group)
                    .error(R.drawable.placeholder_group)
                    .circleCrop()
                    .into(binding.ivGroupCoverSettings)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyGroupSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getStringExtra(EXTRA_GROUP_ID_SETTINGS)
        if (groupId == null) {
            Toast.makeText(this, R.string.group_id_not_provided, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupToolbar()
        setupClickListeners()
        observeViewModel()

        viewModel.loadStudyGroupDetails(groupId!!)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.group_settings)
    }

    private fun setupClickListeners() {
        binding.btnChangeCoverSettings.setOnClickListener {
            openImagePicker()
        }

        binding.btnAddCategorySettings.setOnClickListener {
            addCategoryFromInput()
        }

        binding.btnSaveChangesSettings.setOnClickListener {
            saveChanges()
        }

        binding.btnDeleteGroupSettings.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnManageMembersSettings.setOnClickListener {
            val intent = Intent(this, ManageMembersActivity::class.java).apply {
                putExtra(ManageMembersActivity.EXTRA_GROUP_ID_MANAGE, groupId)
            }
            manageMembersLauncher.launch(intent)
        }

        // Listener para o botão de copiar código
        binding.btnCopyJoinCodeSettings.setOnClickListener {
            copyJoinCodeToClipboard()
        }
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(this) { user ->
            if (user == null) {
                Toast.makeText(this, R.string.user_not_auth, Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            groupId?.let { viewModel.loadStudyGroupDetails(it) }
        }

        viewModel.selectedStudyGroup.observe(this) { group ->
            updateUiWithGroupDetails(group)
        }

        viewModel.isCurrentUserAdmin.observe(this) { isAdmin ->
            updateAdminSpecificUi(isAdmin)
            updateUiWithGroupDetails(viewModel.selectedStudyGroup.value)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarSettings.isVisible = isLoading
            if (isLoading) {
                binding.btnSaveChangesSettings.isEnabled = false
                binding.btnDeleteGroupSettings.isEnabled = false
            } else {
                val isAdmin = viewModel.isCurrentUserAdmin.value == true
                binding.btnSaveChangesSettings.isEnabled = isAdmin
                binding.btnDeleteGroupSettings.isEnabled = isAdmin
            }
        }

        viewModel.updateGroupResultEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                binding.progressBarSettings.isVisible = false
                when (result) {
                    is StudyGroupGeneralResult.Success<*> -> {
                        Toast.makeText(this, getString(R.string.changes_saved_success), Toast.LENGTH_SHORT).show()
                        selectedImageUri = null
                        setResult(Activity.RESULT_OK)
                    }
                    is StudyGroupGeneralResult.Failure -> {
                        Toast.makeText(this, getString(R.string.error_saving_changes, result.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewModel.deleteGroupResultEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                binding.progressBarSettings.isVisible = false
                when (result) {
                    is StudyGroupGeneralResult.Success<*> -> {
                        Toast.makeText(this, getString(R.string.group_deleted_success), Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    is StudyGroupGeneralResult.Failure -> {
                        Toast.makeText(this, getString(R.string.error_deleting_group, result.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewModel.generalEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { studyEvent ->
                if (studyEvent is StudyGroupEvent.Error) {
                    Toast.makeText(this, studyEvent.message, Toast.LENGTH_LONG).show()
                    binding.progressBarSettings.isVisible = false
                    if (studyEvent.message.contains("não encontrado", ignoreCase = true)) {
                        binding.nestedScrollViewSettings.isVisible = false
                        binding.layoutJoinCodeInfoSettings.isVisible = false
                        binding.tvNotAdminMessageSettings.text = studyEvent.message
                        binding.tvNotAdminMessageSettings.isVisible = true
                        if(viewModel.isCurrentUserAdmin.value == false) finish()
                    }
                }
            }
        }
    }

    private fun updateUiWithGroupDetails(group: StudyGroup?) {
        val isAdmin = viewModel.isCurrentUserAdmin.value == true
        if (group != null) {
            if (isAdmin) {
                binding.nestedScrollViewSettings.isVisible = true
                binding.tvNotAdminMessageSettings.isVisible = false
                populateGroupDetails(group)
            } else {
                binding.nestedScrollViewSettings.isVisible = false
                binding.tvNotAdminMessageSettings.isVisible = true
                binding.tvNotAdminMessageSettings.text = getString(R.string.not_admin_message)
            }
            supportActionBar?.title = if(isAdmin) getString(R.string.settings_for_group, group.name) else getString(R.string.view_settings_for_group, group.name)

            // Lógica para mostrar/ocultar o código de acesso
            if (group.isPrivate && isAdmin) {
                binding.layoutJoinCodeInfoSettings.isVisible = true
                if (group.joinCode.isNotBlank()) {
                    binding.tvJoinCodeValueSettings.text = group.joinCode
                    binding.btnCopyJoinCodeSettings.isEnabled = true
                } else {
                    binding.tvJoinCodeValueSettings.text = getString(R.string.join_code_not_available)
                    binding.btnCopyJoinCodeSettings.isEnabled = false
                }
            } else {
                binding.layoutJoinCodeInfoSettings.isVisible = false
            }

        } else if (groupId != null) {
            binding.nestedScrollViewSettings.isVisible = false
            binding.layoutJoinCodeInfoSettings.isVisible = false
            supportActionBar?.title = getString(R.string.group_settings)
        }
    }

    private fun updateAdminSpecificUi(isAdmin: Boolean) {
        // Habilita/desabilita campos editáveis
        binding.btnSaveChangesSettings.isEnabled = isAdmin
        binding.btnDeleteGroupSettings.isEnabled = isAdmin
        binding.btnChangeCoverSettings.isEnabled = isAdmin
        binding.btnAddCategorySettings.isEnabled = isAdmin
        binding.etGroupNameSettings.isEnabled = isAdmin
        binding.etGroupDescriptionSettings.isEnabled = isAdmin
        binding.etNewCategorySettings.isEnabled = isAdmin
        binding.btnManageMembersSettings.isEnabled = isAdmin

        binding.chipGroupCategoriesSettings.children.forEach { view ->
            if (view is Chip) {
                view.isCloseIconVisible = isAdmin
                view.isEnabled = isAdmin
            }
        }
    }


    private fun populateGroupDetails(group: StudyGroup) {
        if (!binding.etGroupNameSettings.hasFocus()) {
            binding.etGroupNameSettings.setText(group.name)
        }
        if(!binding.etGroupDescriptionSettings.hasFocus()){
            binding.etGroupDescriptionSettings.setText(group.description)
        }


        if (selectedImageUri == null) {
            val imageUrl = group.imageUrl.ifEmpty { null }
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_group)
                .error(R.drawable.placeholder_group)
                .circleCrop()
                .into(binding.ivGroupCoverSettings)
        }

        val categoriesToDisplay = group.categories.distinctBy { it.lowercase() }
        if (currentCategories != categoriesToDisplay) {
            currentCategories.clear()
            binding.chipGroupCategoriesSettings.removeAllViews()
            categoriesToDisplay.forEach { category ->
                currentCategories.add(category)
                addCategoryChipVisual(category)
            }
        }
    }

    private fun addCategoryFromInput() {
        val categoryName = binding.etNewCategorySettings.text.toString().trim()
        if (categoryName.isNotBlank() && !currentCategories.any { it.equals(categoryName, ignoreCase = true) }) {
            currentCategories.add(categoryName)
            addCategoryChipVisual(categoryName)
            binding.etNewCategorySettings.text?.clear()
        } else if (categoryName.isNotBlank()) {
            Toast.makeText(this, R.string.category_already_exists, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCategoryChipVisual(categoryName: String) {
        val isAdmin = viewModel.isCurrentUserAdmin.value == true
        val chip = Chip(this).apply {
            text = categoryName
            isCloseIconVisible = isAdmin
            isEnabled = isAdmin

            setOnCloseIconClickListener {
                if (isAdmin) {
                    binding.chipGroupCategoriesSettings.removeView(this)
                    currentCategories.remove(categoryName)
                }
            }
        }
        binding.chipGroupCategoriesSettings.addView(chip)
    }


    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        if (intent.resolveActivity(packageManager) != null) {
            pickImageLauncher.launch(intent)
        } else {
            Toast.makeText(this, R.string.no_app_to_pick_image, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveChanges() {
        if (viewModel.isCurrentUserAdmin.value != true) {
            Toast.makeText(this, R.string.only_admin_can_save_changes, Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.etGroupNameSettings.text.toString().trim()
        val description = binding.etGroupDescriptionSettings.text.toString().trim()

        binding.tilGroupNameSettings.error = null
        if (name.isEmpty()) {
            binding.tilGroupNameSettings.error = getString(R.string.field_required)
            return
        }

        groupId?.let {
            binding.progressBarSettings.isVisible = true
            viewModel.updateStudyGroupDetails(
                groupId = it,
                name = name,
                description = description,
                categories = ArrayList(currentCategories.distinctBy { cat -> cat.lowercase() }),
                newImageUri = selectedImageUri
            )
        }
    }

    private fun showDeleteConfirmationDialog() {
        if (viewModel.isCurrentUserAdmin.value != true) {
            Toast.makeText(this, R.string.only_admin_can_delete_group, Toast.LENGTH_SHORT).show()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_group_confirmation_title))
            .setMessage(getString(R.string.delete_group_confirmation_message))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                groupId?.let {
                    binding.progressBarSettings.isVisible = true
                    viewModel.deleteStudyGroup(it)
                }
            }
            .show()
    }

    private fun copyJoinCodeToClipboard() {
        val joinCode = binding.tvJoinCodeValueSettings.text.toString()

        if (joinCode.isNotBlank() && joinCode != getString(R.string.join_code_not_available)) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("JoinCode", joinCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, R.string.join_code_copied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}