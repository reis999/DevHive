package ipvc.tp.devhive.presentation.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.FragmentProfileBinding
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileEvent
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        loadUserProfile()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.fabEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    viewModel.logout()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                    true
                }
                R.id.action_settings -> {
                    // settings (later)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserProfile() {
        viewModel.loadUserProfile()
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvName.text = user.name
                binding.tvUsername.text = "@${user.username}"
                binding.tvBio.text = user.bio
                binding.tvInstitution.text = user.institution
                binding.tvCourse.text = user.course

                // Estatísticas de contribuição
                binding.tvMaterialsCount.text = user.contributionStats.materials.toString()
                binding.tvCommentsCount.text = user.contributionStats.comments.toString()
                binding.tvLikesCount.text = user.contributionStats.likes.toString()
                binding.tvSessionsCount.text = user.contributionStats.sessions.toString()

                Glide.with(this).load(user.profileImageUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(binding.ivProfile)
            } else {
                requireActivity().finish()
            }
        }

        viewModel.profileEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { profileEvent ->
                when (profileEvent) {
                    is ProfileEvent.Error -> {
                        Toast.makeText(context, profileEvent.message, Toast.LENGTH_SHORT).show()
                    }

                    is ProfileEvent.LogoutSuccess -> {
                        Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
