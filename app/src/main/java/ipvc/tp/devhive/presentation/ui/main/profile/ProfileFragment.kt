package ipvc.tp.devhive.presentation.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.databinding.FragmentProfileBinding
import ipvc.tp.devhive.presentation.ui.profile.EditProfileActivity
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

                // Carregar imagem de perfil (em uma implementação real)
                Glide.with(this).load(user.profileImageUrl).into(binding.ivProfile)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
