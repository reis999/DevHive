package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.FragmentStudyGroupsBinding
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.presentation.ui.main.studygroup.adapters.StudyGroupAdapter
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupEvent
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModel

@AndroidEntryPoint
class StudyGroupsFragment : Fragment(R.layout.fragment_study_groups) {

    private var _binding: FragmentStudyGroupsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudyGroupViewModel by viewModels()
    private lateinit var studyGroupAdapter: StudyGroupAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudyGroupsBinding.bind(view)


        setupRecyclerView()
        setupFab()
        setupObservers()
    }

    private fun setupRecyclerView() {
        studyGroupAdapter = StudyGroupAdapter { clickedStudyGroup ->
            navigateToStudyGroupChat(clickedStudyGroup)
        }
        binding.recyclerView.apply {
            adapter = studyGroupAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun navigateToStudyGroupChat(studyGroup: StudyGroup) {
        val intent = Intent(requireContext(), StudyGroupChatActivity::class.java).apply {
            Log.d("StudyGroupsFragment", "Navigating to Study Group ID: ${studyGroup.id}")
            putExtra(StudyGroupChatActivity.EXTRA_STUDY_GROUP_ID, studyGroup.id)
        }
        startActivity(intent)
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            try {
                findNavController().navigate(R.id.navigation_explore_groups)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, "Erro ao navegar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e("StudyGroupsFragment", "Navigation error", e)
            }
        }
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d("StudyGroupsFragment", "User logged in, loading groups.")
                viewModel.loadUserStudyGroups()
            } else {
                Log.d("StudyGroupsFragment", "User is null or logged out.")
                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.text = getString(R.string.user_not_auth)
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                studyGroupAdapter.submitList(emptyList())
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d("StudyGroupsFragment", "isLoading State: $isLoading")
        }

        viewModel.userStudyGroups.observe(viewLifecycleOwner) { groups ->
            Log.d("StudyGroupsFragment", "User groups count: ${groups.size}")
            studyGroupAdapter.submitList(groups.toList())

            val userIsLoggedIn = viewModel.currentUser.value != null
            if (groups.isEmpty()) {
                if(userIsLoggedIn){
                    binding.tvEmpty.text = getString(R.string.no_study_groups_found)
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.tvEmpty.text = getString(R.string.user_not_auth)
                    binding.tvEmpty.visibility = View.VISIBLE
                }
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.generalEvent.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { studyEvent ->
                if (studyEvent is StudyGroupEvent.Error) {
                    Toast.makeText(context, "Erro: ${studyEvent.message}", Toast.LENGTH_LONG).show()
                    Log.e("StudyGroupsFragment", "General error: ${studyEvent.message}")
                    binding.tvEmpty.text = studyEvent.message
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserStudyGroups()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}