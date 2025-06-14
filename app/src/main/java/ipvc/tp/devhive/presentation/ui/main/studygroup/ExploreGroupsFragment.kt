package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.FragmentExploreGroupsBinding
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.presentation.ui.main.studygroup.adapters.ExploreGroupsAdapter
import ipvc.tp.devhive.presentation.util.EventObserver
import ipvc.tp.devhive.presentation.viewmodel.studygroup.ExploreEvent
import ipvc.tp.devhive.presentation.viewmodel.studygroup.ExploreGroupsViewModel

@AndroidEntryPoint
class ExploreGroupsFragment : Fragment(R.layout.fragment_explore_groups) {

    private val viewModel: ExploreGroupsViewModel by viewModels()
    private var _binding: FragmentExploreGroupsBinding? = null
    private val binding get() = _binding!!
    private lateinit var exploreGroupsAdapter: ExploreGroupsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExploreGroupsBinding.bind(view)

        // Configura a toolbar
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.title_explore_groups)
        }
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        setHasOptionsMenu(true)

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.explore_groups_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_join_private_group -> {
                showEnterJoinCodeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        exploreGroupsAdapter = ExploreGroupsAdapter { studyGroup ->
            handleGroupClick(studyGroup)
        }
        binding.rvExploreGroups.apply {
            adapter = exploreGroupsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFab() {
        binding.fabCreateGroup.setOnClickListener {
            startActivity(Intent(requireContext(), CreateStudyGroupActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.publicGroups.observe(viewLifecycleOwner) { groups ->
            exploreGroupsAdapter.submitList(groups)
            binding.tvNoGroups.visibility = if (groups.isEmpty() && !viewModel.isLoading.value!!) View.VISIBLE else View.GONE
        }

        viewModel.event.observe(viewLifecycleOwner, EventObserver { event ->
            when (event) {
                is ExploreEvent.JoinSuccess -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    // Opcional: Navegar para o chat do grupo ou atualizar a lista de "meus grupos"
                }
                is ExploreEvent.JoinFailure -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun handleGroupClick(studyGroup: StudyGroup) {
        if (studyGroup.isPrivate) {
            showJoinPrivateGroupDialog(studyGroup)
        } else {
            showJoinPublicGroupConfirmationDialog(studyGroup)
        }
    }

    private fun showJoinPublicGroupConfirmationDialog(studyGroup: StudyGroup) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.join_group_title))
            .setMessage(getString(R.string.join_public_group_confirmation, studyGroup.name))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.join)) { dialog, _ ->
                viewModel.joinPublicGroup(studyGroup.id)
                dialog.dismiss()
            }
            .show()
    }


    @SuppressLint("StringFormatInvalid")
    private fun showJoinPrivateGroupDialog(studyGroup: StudyGroup) {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.enter_join_code)
        }
        val dialogLayout = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        editText.layoutParams = params
        dialogLayout.addView(editText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.join_private_group_title, studyGroup.name))
            .setMessage(getString(R.string.enter_join_code_for_group, studyGroup.name))
            .setView(dialogLayout)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.join)) { _, _ ->
                val joinCode = editText.text.toString().trim()
                if (joinCode.isNotEmpty()) {
                    viewModel.joinPrivateGroup(joinCode) // Chama o mesmo método do ViewModel
                } else {
                    Toast.makeText(requireContext(), R.string.join_code_cannot_be_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showEnterJoinCodeDialog() {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.enter_join_code)
        }
        val dialogLayout = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        editText.layoutParams = params
        dialogLayout.addView(editText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.join_private_group_with_code))
            .setMessage(getString(R.string.please_enter_the_join_code))
            .setView(dialogLayout)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.join)) { _, _ ->
                val joinCode = editText.text.toString().trim()
                if (joinCode.isNotEmpty()) {
                    viewModel.joinPrivateGroup(joinCode)
                } else {
                    Toast.makeText(requireContext(), R.string.join_code_cannot_be_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}