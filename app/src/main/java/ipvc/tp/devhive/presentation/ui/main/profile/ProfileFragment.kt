package ipvc.tp.devhive.presentation.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import ipvc.tp.devhive.presentation.ui.main.material.MaterialAdapter
import ipvc.tp.devhive.presentation.ui.main.material.MaterialDetailActivity
import ipvc.tp.devhive.presentation.ui.profile.EditProfileActivity
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModel
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileFragment : Fragment(), MaterialAdapter.OnMaterialClickListener {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var materialViewModel: MaterialViewModel
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvInstitution: TextView
    private lateinit var tvCourse: TextView
    private lateinit var tvMaterialsCount: TextView
    private lateinit var tvCommentsCount: TextView
    private lateinit var tvLikesCount: TextView
    private lateinit var tvSessionsCount: TextView
    private lateinit var recyclerViewMaterials: RecyclerView
    private lateinit var tvNoMaterials: TextView
    private lateinit var fabEditProfile: FloatingActionButton

    private val materialAdapter = MaterialAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragmento
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa as views
        toolbar = view.findViewById(R.id.toolbar)
        ivProfile = view.findViewById(R.id.iv_profile)
        tvName = view.findViewById(R.id.tv_name)
        tvUsername = view.findViewById(R.id.tv_username)
        tvBio = view.findViewById(R.id.tv_bio)
        tvInstitution = view.findViewById(R.id.tv_institution)
        tvCourse = view.findViewById(R.id.tv_course)
        tvMaterialsCount = view.findViewById(R.id.tv_materials_count)
        tvCommentsCount = view.findViewById(R.id.tv_comments_count)
        tvLikesCount = view.findViewById(R.id.tv_likes_count)
        tvSessionsCount = view.findViewById(R.id.tv_sessions_count)
        recyclerViewMaterials = view.findViewById(R.id.recycler_view_materials)
        tvNoMaterials = view.findViewById(R.id.tv_no_materials)
        fabEditProfile = view.findViewById(R.id.fab_edit_profile)

        // Configura a toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        // Configura o RecyclerView
        recyclerViewMaterials.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewMaterials.adapter = materialAdapter

        // Inicializa os ViewModels
        val authFactory = DevHiveApp.getViewModelFactories().authViewModelFactory
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        val materialFactory = DevHiveApp.getViewModelFactories().materialViewModelFactory
        materialViewModel = ViewModelProvider(this, materialFactory)[MaterialViewModel::class.java]

        val profileFactory = DevHiveApp.getViewModelFactories().profileViewModelFactory
        profileViewModel = ViewModelProvider(this, profileFactory)[ProfileViewModel::class.java]

        // Como não temos implementação completa, simulamos dados de perfil
        displayUserProfile(getMockUser())

        // Configura o FAB para editar o perfil
        fabEditProfile.setOnClickListener {
            // Navega para a tela de edição de perfil
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        // Observa a lista de materiais do usuário (simulada)
        // Em uma implementação real, usaríamos materialViewModel.getMaterialsByUser(userId)
        val userMaterials = emptyList<Material>() // Lista vazia para simulação

        if (userMaterials.isEmpty()) {
            tvNoMaterials.visibility = View.VISIBLE
            recyclerViewMaterials.visibility = View.GONE
        } else {
            tvNoMaterials.visibility = View.GONE
            recyclerViewMaterials.visibility = View.VISIBLE
            materialAdapter.submitList(userMaterials)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Navega para as configurações
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.logout) { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout() {
        authViewModel.logout()
        // Navega para a tela de login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun displayUserProfile(user: User) {
        tvName.text = user.name
        tvUsername.text = "@${user.username}"
        tvBio.text = user.bio.ifEmpty { getString(R.string.no_bio) }
        tvInstitution.text = user.institution
        tvCourse.text = user.course

        // Estatísticas de contribuição
        tvMaterialsCount.text = user.contributionStats.materials.toString()
        tvCommentsCount.text = user.contributionStats.comments.toString()
        tvLikesCount.text = user.contributionStats.likes.toString()
        tvSessionsCount.text = user.contributionStats.sessions.toString()

        // Carrega a imagem de perfil
        if (user.profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(ivProfile)
        }
    }

    private fun getMockUser(): User {
        // Simulamos um usuário para fins de demonstração
        return User(
            id = "user123",
            name = "David Reis",
            username = "davidreis",
            email = "david.reis@example.com",
            profileImageUrl = "",
            bio = "Estudante de Engenharia Informática no IPVC. Interessado em desenvolvimento mobile e inteligência artificial.",
            institution = "Instituto Politécnico de Viana do Castelo",
            course = "Licenciatura em Engenharia Informática",
            createdAt = java.util.Date(),
            lastLogin = java.util.Date(),
            isOnline = true,
            contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                materials = 12,
                comments = 45,
                likes = 78,
                sessions = 5
            )
        )
    }

    override fun onMaterialClick(material: Material) {
        // Abre a tela de detalhes do material
        val intent = Intent(requireContext(), MaterialDetailActivity::class.java)
        intent.putExtra(MaterialDetailActivity.EXTRA_MATERIAL_ID, material.id)
        startActivity(intent)
    }

    override fun onBookmarkClick(material: Material, position: Int) {
        // Alterna o estado de favorito do material
        materialViewModel.toggleBookmark(material.id, !material.bookmarked)
    }
}
