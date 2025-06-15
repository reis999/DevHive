package ipvc.tp.devhive.presentation.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.ui.main.material.MaterialAdapter
import ipvc.tp.devhive.presentation.ui.main.material.MaterialDetailActivity
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel
import ipvc.tp.devhive.presentation.viewmodel.profile.ViewedProfileEvent

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity(), MaterialAdapter.OnMaterialClickListener {

    companion object {
        const val EXTRA_CURRENT_USER_ID = "extra_current_user_id"
        const val EXTRA_USER_ID = "extra_user_id"
    }

    private val profileViewModel: ProfileViewModel by viewModels()
    private val materialViewModel: MaterialViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserBio: TextView
    private lateinit var tvMaterialCount: TextView
    private lateinit var tvFollowersCount: TextView
    private lateinit var tvFollowingCount: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerViewMaterials: RecyclerView
    private lateinit var tvNoMaterialsMessage: TextView
    private lateinit var profileLoadingProgressBar: ProgressBar
    private lateinit var materialsListProgressBar: ProgressBar

    private lateinit var materialAdapter: MaterialAdapter
    private var viewedUserId: String = ""
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        viewedUserId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        currentUserId = intent.getStringExtra(EXTRA_CURRENT_USER_ID) ?: ""
        if (viewedUserId.isEmpty()) {
            Toast.makeText(this, "ID de utilizador inválido.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupToolbarAndCollapsingToolbar()

        materialAdapter = MaterialAdapter(this, currentUserId)
        recyclerViewMaterials.layoutManager = GridLayoutManager(this, 2)
        recyclerViewMaterials.adapter = materialAdapter
        recyclerViewMaterials.isNestedScrollingEnabled = false

        setupTabs()
        loadUserProfileData()
        observeViewedUserProfile()
        observeMaterialViewModelLoading()
        observeProfileEvents()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        ivProfileImage = findViewById(R.id.iv_profile_image)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserBio = findViewById(R.id.tv_user_bio)

        // Estatísticas
        tvMaterialCount = findViewById(R.id.tv_material_count)
        tvFollowersCount = findViewById(R.id.tv_followers_count)
        tvFollowingCount = findViewById(R.id.tv_following_count)

        tabLayout = findViewById(R.id.tab_layout)
        recyclerViewMaterials = findViewById(R.id.recycler_view)

        profileLoadingProgressBar = findViewById(R.id.pb_profile_section_loading)
        materialsListProgressBar = findViewById(R.id.pb_materials_section_loading)
        tvNoMaterialsMessage = findViewById(R.id.tv_no_materials_message)

        profileLoadingProgressBar.visibility = View.GONE
        materialsListProgressBar.visibility = View.GONE
        tvNoMaterialsMessage.visibility = View.GONE
    }

    private fun setupToolbarAndCollapsingToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbar.title = " "
    }

    private fun loadUserProfileData() {
        profileViewModel.loadUserProfileById(viewedUserId)
    }

    private fun observeViewedUserProfile() {
        profileViewModel.viewedUserProfile.observe(this) { user ->
            profileLoadingProgressBar.visibility = View.GONE
            if (user != null) {
                displayUserProfileInfo(user)
                if (tabLayout.selectedTabPosition == -1) {
                    tabLayout.getTabAt(0)?.select()
                } else {
                    handleTabSelection(tabLayout.selectedTabPosition)
                }
            }
        }

        profileViewModel.isLoadingViewedProfile.observe(this) { isLoading ->
            profileLoadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if(isLoading) {
                findViewById<LinearLayout>(R.id.appBarLayout).alpha = 0.3f // Exemplo
            } else {
                findViewById<LinearLayout>(R.id.appBarLayout).alpha = 1.0f
            }
        }
    }

    private fun displayUserProfileInfo(user: User) {
        tvUserName.text = user.name
        tvUserBio.text = user.bio.ifEmpty { getString(R.string.no_bio_available) }

        tvMaterialCount.text = user.contributionStats.materials.toString()
        tvFollowersCount.text = user.contributionStats.likes.toString()
        tvFollowingCount.text = user.contributionStats.comments.toString()

        Glide.with(this)
            .load(user.profileImageUrl)
            .placeholder(R.drawable.profile_placeholder)
            .error(R.drawable.profile_placeholder)
            .into(ivProfileImage)
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.materials))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.favorites))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                handleTabSelection(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                handleTabSelection(tab?.position ?: 0)
            }
        })
    }

    private fun handleTabSelection(position: Int) {
        if (profileViewModel.viewedUserProfile.value == null && profileViewModel.isLoadingViewedProfile.value == false) {
            return
        }
        materialAdapter.submitList(emptyList())
        tvNoMaterialsMessage.visibility = View.GONE
        recyclerViewMaterials.visibility = View.GONE

        when (position) {
            0 -> loadUserCreatedMaterials()
            1 -> loadUserFavoriteMaterials()
        }
    }

    private fun loadUserCreatedMaterials() {
        recyclerViewMaterials.visibility = View.GONE
        materialViewModel.getMaterialsByUser(viewedUserId).observe(this) { materials ->
            displayMaterialsInList(materials, isUserMaterials = true)
        }
    }

    private fun loadUserFavoriteMaterials() {
        recyclerViewMaterials.visibility = View.GONE
        materialViewModel.getBookmarkedMaterials(viewedUserId).observe(this) { materials ->
            displayMaterialsInList(materials, isUserMaterials = false)
        }
    }

    private fun displayMaterialsInList(materials: List<Material>, isUserMaterials: Boolean) {
        if (materials.isEmpty()) {
            tvNoMaterialsMessage.visibility = View.VISIBLE
            recyclerViewMaterials.visibility = View.GONE
            val messageResId = if (isUserMaterials) {
                R.string.no_materials_created_by_this_user
            } else {
                R.string.this_user_has_no_favorites
            }
            tvNoMaterialsMessage.text = getString(messageResId)
        } else {
            tvNoMaterialsMessage.visibility = View.GONE
            recyclerViewMaterials.visibility = View.VISIBLE
            materialAdapter.submitList(materials)
        }
    }

    private fun observeMaterialViewModelLoading() {
        materialViewModel.isLoading.observe(this) { isLoading ->
            materialsListProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                tvNoMaterialsMessage.visibility = View.GONE
                recyclerViewMaterials.visibility = View.GONE
            }
        }
    }

    private fun observeProfileEvents() {
        profileViewModel.viewedProfileEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { viewedEvent ->
                when (viewedEvent) {
                    is ViewedProfileEvent.Error -> {
                        Toast.makeText(this, viewedEvent.message, Toast.LENGTH_LONG).show()
                        if (profileViewModel.viewedUserProfile.value == null) {
                            tvUserName.text = getString(R.string.error_loading_profile)
                            tvUserBio.text = viewedEvent.message
                        }
                    }
                }
            }
        }
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

    override fun onMaterialClick(material: Material) {
        val intent = Intent(this, MaterialDetailActivity::class.java)
        intent.putExtra(MaterialDetailActivity.EXTRA_MATERIAL_ID, material.id)
        startActivity(intent)
    }

    override fun onBookmarkClick(material: Material, position: Int) {
        TODO("Not yet implemented")
    }
}