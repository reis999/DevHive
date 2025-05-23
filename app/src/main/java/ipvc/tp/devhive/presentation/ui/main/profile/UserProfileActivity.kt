package ipvc.tp.devhive.presentation.ui.main.profile

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.ui.main.material.MaterialAdapter
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel
import com.google.android.material.tabs.TabLayout

class UserProfileActivity : AppCompatActivity(), MaterialAdapter.OnMaterialClickListener {

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }

    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserBio: TextView
    private lateinit var tvMaterialCount: TextView
    private lateinit var tvFollowersCount: TextView
    private lateinit var tvFollowingCount: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView

    private val materialAdapter = MaterialAdapter(this)
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Obtém o ID do usuário da intent
        userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        if (userId.isEmpty()) {
            finish()
            return
        }

        // Inicializa as views
        toolbar = findViewById(R.id.toolbar)
        ivProfileImage = findViewById(R.id.iv_profile_image)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserBio = findViewById(R.id.tv_user_bio)
        tvMaterialCount = findViewById(R.id.tv_material_count)
        tvFollowersCount = findViewById(R.id.tv_followers_count)
        tvFollowingCount = findViewById(R.id.tv_following_count)
        tabLayout = findViewById(R.id.tab_layout)
        recyclerView = findViewById(R.id.recycler_view)

        // Configura a toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configura o RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = materialAdapter

        // Inicializa o ViewModel
        val factory = DevHiveApp.getViewModelFactories().profileViewModelFactory
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        // Carrega os dados do usuário
        loadUserProfile()

        // Configura as tabs
        setupTabs()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUserProfile() {
        // Em uma implementação real, usaríamos profileViewModel.getUserById(userId)
        // Para fins de demonstração, usamos dados simulados
        val mockUser = getMockUser()
        displayUserProfile(mockUser)

        val mockMaterials = getMockUserMaterials()
        materialAdapter.submitList(mockMaterials)
    }

    private fun displayUserProfile(user: User) {
        supportActionBar?.title = user.name

        tvUserName.text = user.name
        tvUserBio.text = user.bio
        tvMaterialCount.text = "12" // Em uma implementação real, viria do backend
        tvFollowersCount.text = "156"
        tvFollowingCount.text = "89"

        // Carrega a imagem do perfil
        if (user.profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(ivProfileImage)
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.materials))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.favorites))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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

    private fun loadUserMaterials() {
        // Carrega os materiais do usuário
        val mockMaterials = getMockUserMaterials()
        materialAdapter.submitList(mockMaterials)
    }

    private fun loadUserFavorites() {
        // Carrega os materiais favoritos do usuário
        val mockFavorites = getMockUserFavorites()
        materialAdapter.submitList(mockFavorites)
    }

    override fun onMaterialClick(material: Material) {
        // Implementa a navegação para os detalhes do material
    }

    override fun onBookmarkClick(material: Material, position: Int) {
        // Implementa a ação de favoritar/desfavoritar
    }

    private fun getMockUser(): User {
        return User(
            id = userId,
            name = "Ana Silva",
            username = "ana_silva",
            email = "ana.silva@email.com",
            bio = "Estudante de Engenharia Informática apaixonada por tecnologia e programação.",
            profileImageUrl = "",
            institution = "Instituto Politécnico de Viana do Castelo",
            course = "Engenharia Informática",
            createdAt = java.util.Date(),
            lastLogin = java.util.Date(),
            isOnline = false,
            contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                materials = 2,
                comments = 10,
                likes = 5,
                sessions = 3
            )
        )
    }

    private fun getMockUserMaterials(): List<Material> {
        // Retorna uma lista simulada de materiais do usuário
        return emptyList()
    }

    private fun getMockUserFavorites(): List<Material> {
        // Retorna uma lista simulada de materiais favoritos do usuário
        return emptyList()
    }
}
