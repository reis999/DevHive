package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ActivityStudyGroupChatBinding
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.presentation.ui.main.studygroup.adapters.GroupMessageAdapter
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupEvent
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupGeneralResult
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModel

@AndroidEntryPoint
class StudyGroupChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDY_GROUP_ID = "extra_study_group_id"
    }

    private lateinit var binding: ActivityStudyGroupChatBinding
    private lateinit var attachmentPreviewLayout: ConstraintLayout
    private lateinit var ivAttachmentPreviewIcon: ImageView
    private lateinit var tvAttachmentPreviewName: TextView
    private lateinit var btnRemoveAttachment: ImageButton
    private val viewModel: StudyGroupViewModel by viewModels()

    private lateinit var groupMessageAdapter: GroupMessageAdapter
    private var currentUserId: String? = null
    private var studyGroupId: String = ""
    private var isAdapterInitialized = false
    private var pendingAttachmentUri: Uri? = null
    private var pendingAttachmentFileName: String? = null

    private val groupSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, R.string.group_settings_updated, Toast.LENGTH_SHORT).show()
            viewModel.loadStudyGroupDetails(studyGroupId)
        }
    }

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            handleSelectedFile(it)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        attachmentPreviewLayout = binding.attachmentPreviewLayout
        ivAttachmentPreviewIcon = binding.ivAttachmentPreviewIcon
        tvAttachmentPreviewName = binding.tvAttachmentPreviewName
        btnRemoveAttachment = binding.btnRemoveAttachment

        studyGroupId = intent.getStringExtra(EXTRA_STUDY_GROUP_ID) ?: ""
        if (studyGroupId.isEmpty()) {
            Toast.makeText(this, R.string.group_id_not_provided, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        observeViewModel()

        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.btnAttachFile.setOnClickListener {
            openFilePicker()
        }

        btnRemoveAttachment.setOnClickListener {
            clearAttachmentPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStudyGroupDetails(studyGroupId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun initializeAdapterAndRecyclerView(userId: String) {
        if (!isAdapterInitialized || currentUserId != userId) {
            currentUserId = userId
            groupMessageAdapter = GroupMessageAdapter(userId)
            binding.recyclerViewMessages.apply {
                layoutManager = LinearLayoutManager(this@StudyGroupChatActivity).apply {
                    stackFromEnd = true
                }
                adapter = groupMessageAdapter
            }
            isAdapterInitialized = true
            Log.d("ChatActivity", "Adapter initialized/updated with userId: $userId")

            groupMessageAdapter.setOnDownloadClickListener { url, fileName, mimeType ->
                startDownload(url, fileName, mimeType)
            }

            if (studyGroupId.isNotEmpty()) {
                viewModel.loadGroupMessages(studyGroupId)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(this) { user ->
            if (user != null) {
                initializeAdapterAndRecyclerView(user.id)

                if (studyGroupId.isNotEmpty()) {
                    viewModel.loadStudyGroupDetails(studyGroupId)
                }
            } else {
                Toast.makeText(this, R.string.user_not_auth, Toast.LENGTH_LONG).show()
                isAdapterInitialized = false
                currentUserId = null
                finish()
            }
        }

        viewModel.selectedStudyGroup.observe(this) { group ->
            if (group != null) {
                displayStudyGroupDetails(group)
                invalidateOptionsMenu()
            } else {
                if (viewModel.currentUser.value != null && isAdapterInitialized) {
                    Toast.makeText(this, R.string.group_no_longer_available, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        viewModel.groupMessages.observe(this) { messages ->
            if (isAdapterInitialized) {
                displayMessages(messages)
            }
        }

        viewModel.sendMessageResultEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                binding.btnSend.isEnabled = true
                when (result) {
                    is StudyGroupGeneralResult.Success<*> -> {
                        binding.etMessage.text?.clear()
                        pendingAttachmentUri = null
                        pendingAttachmentFileName = null
                        binding.btnAttachFile.clearColorFilter()
                        clearAttachmentPreview()
                        Log.d("ChatActivity", "Mensagem enviada com sucesso.")
                    }
                    is StudyGroupGeneralResult.Failure -> {
                        Toast.makeText(this, getString(R.string.error_sending_message_param, result.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.generalEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { studyEvent ->
                if (studyEvent is StudyGroupEvent.Error) {
                    Toast.makeText(this, studyEvent.message, Toast.LENGTH_LONG).show()
                    if (studyEvent.message.contains("não encontrado", ignoreCase = true) ||
                        studyEvent.message.contains("not found", ignoreCase = true)) {
                        finish()
                    }
                }
            }
        }

        viewModel.isCurrentUserAdmin.observe(this) {
            invalidateOptionsMenu()
        }

        // --- NOVO OBSERVADOR PARA O RESULTADO DE SAIR DO GRUPO ---
        viewModel.leaveGroupResultEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is StudyGroupGeneralResult.Success<*> -> {
                        Toast.makeText(this, R.string.you_left_group_success, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is StudyGroupGeneralResult.Failure -> {
                        Toast.makeText(this, getString(R.string.error_leaving_group, result.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun displayStudyGroupDetails(studyGroup: StudyGroup) {
        supportActionBar?.title = studyGroup.name
        binding.tvGroupName.text = studyGroup.name
        binding.tvMemberCount.text = getString(R.string.member_count, studyGroup.members.size)

        val imageUrl = studyGroup.imageUrl.ifEmpty { null }
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_group)
            .error(R.drawable.placeholder_group)
            .circleCrop()
            .into(binding.ivGroupImage)
    }

    private fun displayMessages(messages: List<GroupMessage>) {
        if (!::groupMessageAdapter.isInitialized) return

        val previousItemCount = groupMessageAdapter.itemCount
        groupMessageAdapter.submitList(messages.toList()) {
            if (messages.isNotEmpty()) {
                val layoutManager = binding.recyclerViewMessages.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition == previousItemCount - 1 || previousItemCount == 0 || groupMessageAdapter.itemCount > previousItemCount) {
                    scrollToLastMessage()
                }
            }
        }
        Log.d("ChatActivity", "Displaying ${messages.size} messages.")
    }

    private fun scrollToLastMessage() {
        if (groupMessageAdapter.itemCount > 0) {
            binding.recyclerViewMessages.smoothScrollToPosition(groupMessageAdapter.itemCount - 1)
        }
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()

        if (messageText.isEmpty() && pendingAttachmentUri == null) {
            return
        }

        binding.btnSend.isEnabled = false

        viewModel.sendGroupMessageWithAttachment(
            groupId = studyGroupId,
            content = messageText,
            attachmentUri = pendingAttachmentUri,
            originalAttachmentFileName = pendingAttachmentFileName
        )
    }

    private fun openFilePicker() {
        selectFileLauncher.launch("*/*")
    }

    private fun handleSelectedFile(uri: Uri) {
        pendingAttachmentUri = uri
        pendingAttachmentFileName = getFileName(uri)

        if (pendingAttachmentFileName != null) {
            tvAttachmentPreviewName.text = pendingAttachmentFileName
            attachmentPreviewLayout.visibility = View.VISIBLE
            binding.btnAttachFile.setColorFilter(ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_IN)
        } else {
            clearAttachmentPreview()
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                name = name?.substring(cut + 1)
            }
        }
        return name
    }

    private fun clearAttachmentPreview() {
        pendingAttachmentUri = null
        pendingAttachmentFileName = null
        attachmentPreviewLayout.visibility = View.GONE
        tvAttachmentPreviewName.text = ""
        binding.btnAttachFile.clearColorFilter()
    }

    private fun startDownload(url: String, fileName: String, mimeType: String) {
        try {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(url.toUri())
                .setTitle(fileName)
                .setDescription(getString(R.string.downloading_file))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType(mimeType)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            downloadManager.enqueue(request)
            Toast.makeText(this, getString(R.string.download_started_param, fileName), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // ...
        }
    }

    // --- FUNÇÃO DE CONFIRMAÇÃO PARA SAIR DO GRUPO ---
    private fun showLeaveGroupConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.leave_group_confirmation_title))
            .setMessage(getString(R.string.leave_group_confirmation_message))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.leave_group)) { _, _ ->
                viewModel.leaveStudyGroup(studyGroupId)
            }
            .show()
    }

    // --- ATUALIZADO ---
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.study_group_chat_menu, menu)

        val settingsAdminItem = menu.findItem(R.id.action_group_settings)
        val leaveGroupMemberItem = menu.findItem(R.id.action_chat_leave_group_member)

        val group = viewModel.selectedStudyGroup.value
        val currentUser = viewModel.currentUser.value
        val isAdmin = viewModel.isCurrentUserAdmin.value == true

        if (group != null && currentUser != null) {
            val isMember = group.members.contains(currentUser.id) || group.admins.contains(currentUser.id) // Admins também são membros

            settingsAdminItem?.isVisible = isAdmin
            leaveGroupMemberItem?.isVisible = !isAdmin && isMember
        } else {
            settingsAdminItem?.isVisible = false
            leaveGroupMemberItem?.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_group_settings -> {
                if (viewModel.isCurrentUserAdmin.value == true && studyGroupId.isNotEmpty()) {
                    val intent = Intent(this, StudyGroupSettingsActivity::class.java).apply {
                        putExtra(StudyGroupSettingsActivity.EXTRA_GROUP_ID_SETTINGS, studyGroupId)
                    }
                    groupSettingsLauncher.launch(intent)
                } else {
                    Toast.makeText(this, R.string.only_admin_can_access_settings, Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_chat_leave_group_member -> {

                val group = viewModel.selectedStudyGroup.value
                val currentUser = viewModel.currentUser.value
                if (group != null && currentUser != null && (group.members.contains(currentUser.id) || group.admins.contains(currentUser.id)) && viewModel.isCurrentUserAdmin.value == false) {
                    showLeaveGroupConfirmationDialog()
                } else {
                    // Caso raro
                    Toast.makeText(this, R.string.only_members_can_leave, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}