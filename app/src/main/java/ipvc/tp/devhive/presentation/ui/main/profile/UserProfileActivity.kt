package ipvc.tp.devhive.presentation.ui.main.profile

import android.content.Intent
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
import ipvc.tp.devhive.presentation.ui.main.material.MaterialDetailActivity
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp

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
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Obtém o ID do usuário da intent
        userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        if (userId.isEmpty()) {
            finish()
            return
        }

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        initializeViewModel()
        setupTabs()
        loadUserProfile()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        ivProfileImage = findViewById(R.id.iv_profile_image)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserBio = findViewById(R.id.tv_user_bio)
        tvMaterialCount = findViewById(R.id.tv_material_count)
        tvFollowersCount = findViewById(R.id.tv_followers_count)
        tvFollowingCount = findViewById(R.id.tv_following_count)
        tabLayout = findViewById(R.id.tab_layout)
        recyclerView = findViewById(R.id.recycler_view)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = materialAdapter
    }

    private fun initializeViewModel() {
        val factory = DevHiveApp.getViewModelFactories().profileViewModelFactory
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
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
        // implementação real: usar profileViewModel.getUserById(userId)
        val mockUser = getMockUser()
        displayUserProfile(mockUser)

        // Carrega os materiais do utilizador por padrão
        loadUserMaterials()
    }

    private fun displayUserProfile(user: User) {
        currentUser = user
        supportActionBar?.title = user.name

        tvUserName.text = user.name
        tvUserBio.text = user.bio.ifEmpty { getString(R.string.no_bio) }

        // implementação real: estes valores viriam do backend
        tvMaterialCount.text = "12"
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
        } else {
            ivProfileImage.setImageResource(R.drawable.profile_placeholder)
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
        // implementação real: usar profileViewModel.getUserMaterials(userId)
        val mockMaterials = getMockUserMaterials()
        materialAdapter.submitList(mockMaterials)
    }

    private fun loadUserFavorites() {
        // implementação real: usar profileViewModel.getUserFavorites(userId)
        val mockFavorites = getMockUserFavorites()
        materialAdapter.submitList(mockFavorites)
    }

    override fun onMaterialClick(material: Material) {
        val intent = Intent(this, MaterialDetailActivity::class.java)
        intent.putExtra(MaterialDetailActivity.EXTRA_MATERIAL_ID, material.id)
        startActivity(intent)
    }

    override fun onBookmarkClick(material: Material, position: Int) {
        // implementação real: usar materialViewModel.toggleBookmark()
        val updatedMaterial = material.copy(bookmarked = !material.bookmarked)
        val currentList = materialAdapter.currentList.toMutableList()
        currentList[position] = updatedMaterial
        materialAdapter.submitList(currentList)
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
            createdAt = Timestamp(java.util.Date()),
            lastLogin = Timestamp(java.util.Date()),
            isOnline = true,
            contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                materials = 4,
                comments = 12,
                likes = 8,
                sessions = 4
            )

        )
    }

    private fun getMockUserMaterials(): List<Material> {
        // implementação real: retornar materiais do utilizador
        return listOf(
            Material(
                id = "material1",
                title = "Guia de Kotlin",
                description = "Guia completo sobre Kotlin",
                ownerUid = userId,
                authorName = "Ana Silva",
                authorImageUrl = "",
                thumbnailUrl = "",
                contentUrl = "",
                type = "pdf",
                fileSize = 1024,
                categories = listOf("Kotlin", "Programação"),
                subject = "Programação",
                downloads = 45,
                views = 123,
                likes = 28,
                bookmarked = false,
                createdAt = Timestamp(java.util.Date()),
                updatedAt = Timestamp(java.util.Date()),
                isPublic = true,
                rating = 4.2f,
                reviewCount = 15
            )
        )
    }

    private fun getMockUserFavorites(): List<Material> {
        // implementação real: retornar materiais favoritos do utilizador
        return listOf(
            Material(
                id = "material2",
                title = "Android Development",
                description = "Curso de desenvolvimento Android",
                ownerUid = "other_user",
                authorName = "João Santos",
                authorImageUrl = "",
                thumbnailUrl = "",
                contentUrl = "",
                type = "pdf",
                fileSize = 2048,
                categories = listOf("Android", "Mobile"),
                subject = "Desenvolvimento Mobile",
                downloads = 67,
                views = 201,
                likes = 42,
                bookmarked = true,
                createdAt = Timestamp(java.util.Date()),
                updatedAt = Timestamp(java.util.Date()),
                isPublic = true,
                rating = 4.5f,
                reviewCount = 30
            )
        )
    }
}
