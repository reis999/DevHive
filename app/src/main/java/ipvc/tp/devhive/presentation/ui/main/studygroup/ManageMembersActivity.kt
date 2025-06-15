package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ActivityManageMembersBinding
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.ui.main.studygroup.adapters.MemberAdapter
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupEvent
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupGeneralResult
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModel

@AndroidEntryPoint
class ManageMembersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageMembersBinding
    private val viewModel: StudyGroupViewModel by viewModels()
    private lateinit var memberAdapter: MemberAdapter

    private var groupId: String? = null
    private var currentGroup: StudyGroup? = null
    private var currentUserId: String? = null

    companion object {
        const val EXTRA_GROUP_ID_MANAGE = "extra_group_id_manage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getStringExtra(EXTRA_GROUP_ID_MANAGE)
        if (groupId == null) {
            Toast.makeText(this, R.string.group_id_not_provided, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupToolbar()
        observeViewModel()

        viewModel.loadStudyGroupDetails(groupId!!)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarManageMembers)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.manage_members_title)
    }

    private fun setupRecyclerView() {
        val groupAdmins = currentGroup?.admins ?: emptyList()
        val viewerId = currentUserId ?: ""

        memberAdapter = MemberAdapter(
            onKickClickListener = { userToKick ->
                showKickConfirmationDialog(userToKick)
            },
            currentUserId = viewerId,
            groupAdminIds = groupAdmins
        )

        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(this@ManageMembersActivity)
            adapter = memberAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(this) { user ->
            if (user == null) {
                Toast.makeText(this, R.string.user_not_auth, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                currentUserId = user.id
                if (currentGroup == null && groupId != null) {
                    viewModel.loadStudyGroupDetails(groupId!!)
                } else if (::memberAdapter.isInitialized) {
                    currentGroup?.let { updateAdapterWithLatestData(it, viewModel.groupMembersDetails.value ?: emptyList()) }
                }
            }
        }

        viewModel.selectedStudyGroup.observe(this) { group ->
            if (group == null && groupId != null) {
                Toast.makeText(this, R.string.group_not_found, Toast.LENGTH_SHORT).show()
                finish()
                return@observe
            }
            if (group != null && group.id == groupId) {
                currentGroup = group
                if (currentUserId != null) {
                    setupRecyclerView()
                }

                val allMemberIds = (group.members + group.admins).distinct()
                if (allMemberIds.isNotEmpty()) {
                    viewModel.loadGroupMembersDetails(allMemberIds)
                } else {
                    binding.tvNoMembersToManage.isVisible = true
                    binding.rvMembers.isVisible = false
                    memberAdapter.submitList(emptyList())
                }
            }
        }

        viewModel.groupMembersDetails.observe(this) { membersList ->
            currentGroup?.let { group ->
                updateAdapterWithLatestData(group, membersList)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarManageMembers.isVisible = isLoading
        }

        viewModel.removeMemberResultEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is StudyGroupGeneralResult.Success<*> -> {
                        Toast.makeText(this, R.string.member_removed_success, Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                    }
                    is StudyGroupGeneralResult.Failure -> {
                        Toast.makeText(this, getString(R.string.error_removing_member, result.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewModel.generalEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { studyEvent ->
                if (studyEvent is StudyGroupEvent.Error) {
                    Toast.makeText(this, studyEvent.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateAdapterWithLatestData(group: StudyGroup, membersDetails: List<User>) {
        if (!::memberAdapter.isInitialized) {
            if (currentUserId != null) setupRecyclerView() else return
        }

        if (memberAdapter.groupAdminIds != group.admins || memberAdapter.currentUserId != currentUserId) {
            Log.d("ManageMembersActivity", "Adapter params changed or currentGroup updated, re-setting up RecyclerView.")
            setupRecyclerView()
        }
        Log.d("ManageMembersActivity", "Updating adapter. Group members from selectedGroup: ${group.members.size}, Member details loaded: ${membersDetails.size}")


        if (membersDetails.isEmpty() && (group.members.isNotEmpty() || group.admins.isNotEmpty())) {
            binding.tvNoMembersToManage.isVisible = false
        } else if (membersDetails.isEmpty()) {
            binding.tvNoMembersToManage.isVisible = true
            binding.rvMembers.isVisible = false
        } else {
            binding.tvNoMembersToManage.isVisible = false
            binding.rvMembers.isVisible = true
        }
        memberAdapter.submitList(membersDetails)
    }


    private fun showKickConfirmationDialog(memberToKick: User) {
        val groupAdmins = currentGroup?.admins ?: emptyList()

        if (groupAdmins.contains(memberToKick.id)) {
            Toast.makeText(this, R.string.cannot_kick_admin, Toast.LENGTH_SHORT).show()
            return
        }
        if (memberToKick.id == currentUserId) {
            Toast.makeText(this, R.string.cannot_kick_self, Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.kick_member_confirmation_title))
            .setMessage(getString(R.string.kick_member_confirmation_message, memberToKick.name))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.kick_member)) { _, _ ->
                groupId?.let { gid ->
                    viewModel.removeMemberFromGroup(gid, memberToKick.id)
                }
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}