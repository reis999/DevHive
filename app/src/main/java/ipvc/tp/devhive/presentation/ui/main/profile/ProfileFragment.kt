package ipvc.tp.devhive.presentation.ui.main.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.FragmentProfileBinding
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import ipvc.tp.devhive.presentation.ui.main.material.MaterialAdapter
import ipvc.tp.devhive.presentation.ui.main.material.MaterialDetailActivity
import ipvc.tp.devhive.presentation.ui.profile.EditProfileActivity
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel
import androidx.appcompat.app.AlertDialog

@AndroidEntryPoint
class ProfileFragment : Fragment(), MaterialAdapter.OnMaterialClickListener {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()
    private val materialViewModel: MaterialViewModel by viewModels()

    private lateinit var materialAdapter: MaterialAdapter
    private var currentUserId: String? = null

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

        setupRecyclerView()
        setupTabs()
        setupMenu()
        setupListeners()
        loadUserProfile()
        observeViewModel()
    }

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            profileViewModel.loadUserProfile()
        }
    }

    private fun setupRecyclerView() {
        materialAdapter = MaterialAdapter(this, null)
        binding.recyclerViewMaterials.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMaterials.adapter = materialAdapter
    }

    private fun setupTabs() {
        // Adiciona as tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.my_materials))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.my_favorites))

        // Configura o listener das tabs
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadUserMaterials()
                    1 -> loadUserFavorites()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupMenu() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    Toast.makeText(requireContext(), "Configurações", Toast.LENGTH_SHORT).show()
                    // TODO: Implementar navegação para configurações
                    true
                }
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Saída")
            .setMessage("Tem a certeza que pretende sair?")
            .setPositiveButton("Sair") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        try {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            requireActivity().finish()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erro ao fazer logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.fabEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }
    }

    private fun loadUserProfile() {
        profileViewModel.loadUserProfile()
    }

    // Função para carregar materiais do utilizador (Tab 1)
    private fun loadUserMaterials() {
        currentUserId?.let { userId ->
            materialViewModel.getMaterialsByUser(userId).observe(viewLifecycleOwner) { materials ->
                displayMaterials(materials, isUserMaterials = true)
            }
        }
    }

    // Função para carregar favoritos do utilizador (Tab 2)
    private fun loadUserFavorites() {
        currentUserId?.let { userId ->
            materialViewModel.getBookmarkedMaterials(userId).observe(viewLifecycleOwner) { materials ->
                displayMaterials(materials, isUserMaterials = false)
            }
        }
    }

    // Função para exibir materiais no RecyclerView
    private fun displayMaterials(materials: List<Material>, isUserMaterials: Boolean) {
        if (materials.isEmpty()) {
            binding.tvNoMaterials.visibility = View.VISIBLE
            binding.recyclerViewMaterials.visibility = View.GONE

            // Atualiza a mensagem baseado na tab selecionada
            val messageResId = if (isUserMaterials) {
                R.string.no_materials_created
            } else {
                R.string.no_favorites_yet
            }
            binding.tvNoMaterials.text = getString(messageResId)
        } else {
            binding.tvNoMaterials.visibility = View.GONE
            binding.recyclerViewMaterials.visibility = View.VISIBLE
            materialAdapter.submitList(materials)
        }
    }

    private fun observeViewModel() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUserId = user.id

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

                // Carregar imagem de perfil
                Glide.with(this)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(binding.ivProfile)

                materialAdapter = MaterialAdapter(this, user.id)
                binding.recyclerViewMaterials.adapter = materialAdapter

                loadUserMaterials()
            }
        }
    }

    // Implementação do MaterialAdapter.OnMaterialClickListener
    override fun onMaterialClick(material: Material) {
        val intent = Intent(requireContext(), MaterialDetailActivity::class.java)
        intent.putExtra(MaterialDetailActivity.EXTRA_MATERIAL_ID, material.id)
        startActivity(intent)
    }

    override fun onBookmarkClick(material: Material, position: Int) {
        val currentUserId = materialViewModel.getCurrentUserId()

        if (currentUserId != null) {
            val isCurrentlyBookmarked = currentUserId in material.bookmarkedBy

            materialViewModel.toggleBookmark(
                materialId = material.id,
                userId = currentUserId,
                isBookmarked = !isCurrentlyBookmarked
            )

            val selectedTab = binding.tabLayout.selectedTabPosition
            if (selectedTab == 1) {
                loadUserFavorites()
            }
        } else {
            Toast.makeText(requireContext(), "É necessário fazer login para marcar favoritos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}