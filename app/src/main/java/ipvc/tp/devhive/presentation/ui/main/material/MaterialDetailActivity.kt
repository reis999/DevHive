package ipvc.tp.devhive.presentation.ui.main.material

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.presentation.ui.main.comment.CommentAdapter
import ipvc.tp.devhive.presentation.ui.main.comment.CreateCommentBottomSheet
import ipvc.tp.devhive.presentation.ui.main.profile.UserProfileActivity
import ipvc.tp.devhive.presentation.util.DateFormatUtils
import ipvc.tp.devhive.presentation.viewmodel.comment.CommentViewModel
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import java.util.Date

class MaterialDetailActivity : AppCompatActivity(), CommentAdapter.OnCommentClickListener {

    companion object {
        const val EXTRA_MATERIAL_ID = "extra_material_id"
    }

    private lateinit var materialViewModel: MaterialViewModel
    private lateinit var commentViewModel: CommentViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var ivMaterialCover: ImageView
    private lateinit var ivAuthorAvatar: ImageView
    private lateinit var tvAuthorName: TextView
    private lateinit var tvUploadDate: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvDownloads: TextView
    private lateinit var tvViews: TextView
    private lateinit var tvLikes: TextView
    private lateinit var chipGroupTags: ChipGroup
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var tvNoComments: TextView
    private lateinit var fabAddComment: FloatingActionButton

    private val commentAdapter = CommentAdapter(this)
    private var materialId: String = ""
    private var currentMaterial: Material? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_detail)

        // Obtém o ID do material da intent
        materialId = intent.getStringExtra(EXTRA_MATERIAL_ID) ?: ""
        if (materialId.isEmpty()) {
            Toast.makeText(this, R.string.error_loading_material, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        initializeViewModels()
        loadMaterialDetails()
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        ivMaterialCover = findViewById(R.id.iv_material_cover)
        ivAuthorAvatar = findViewById(R.id.iv_author_avatar)
        tvAuthorName = findViewById(R.id.tv_author_name)
        tvUploadDate = findViewById(R.id.tv_upload_date)
        tvDescription = findViewById(R.id.tv_description)
        tvDownloads = findViewById(R.id.tv_downloads)
        tvViews = findViewById(R.id.tv_views)
        tvLikes = findViewById(R.id.tv_likes)
        chipGroupTags = findViewById(R.id.chip_group_tags)
        recyclerViewComments = findViewById(R.id.recycler_view_comments)
        tvNoComments = findViewById(R.id.tv_no_comments)
        fabAddComment = findViewById(R.id.fab_add_comment)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        recyclerViewComments.adapter = commentAdapter
    }

    private fun initializeViewModels() {
        val viewModelFactories = DevHiveApp.getViewModelFactories()

        materialViewModel = ViewModelProvider(
            this,
            viewModelFactories.materialViewModelFactory
        )[MaterialViewModel::class.java]

        commentViewModel = ViewModelProvider(
            this,
            viewModelFactories.commentViewModelFactory
        )[CommentViewModel::class.java]
    }

    private fun setupClickListeners() {
        fabAddComment.setOnClickListener {
            showAddCommentDialog()
        }

        val authorContainer = findViewById<View>(R.id.container_author)
        authorContainer.setOnClickListener {
            currentMaterial?.let {
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra(UserProfileActivity.EXTRA_USER_ID, it.ownerUid)
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.material_detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_bookmark -> {
                toggleBookmark()
                true
            }
            R.id.action_download -> {
                downloadMaterial()
                true
            }
            R.id.action_share -> {
                shareMaterial()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadMaterialDetails() {
        // implementação real: usar materialViewModel.getMaterialById(materialId)
        val mockMaterial = getMockMaterial()
        displayMaterialDetails(mockMaterial)

        // Carrega os comentários do material
        val mockComments = getMockComments()
        displayComments(mockComments)
    }

    private fun displayMaterialDetails(material: Material) {
        currentMaterial = material

        collapsingToolbar.title = material.title

        // Carrega a imagem de capa
        if (material.thumbnailUrl.isNotEmpty()) {
            Glide.with(this)
                .load(material.thumbnailUrl)
                .placeholder(R.drawable.material_placeholder)
                .error(R.drawable.material_placeholder)
                .into(ivMaterialCover)
        }

        // Carrega a imagem do autor
        if (material.authorImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(material.authorImageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(ivAuthorAvatar)
        }

        tvAuthorName.text = material.authorName
        tvUploadDate.text = DateFormatUtils.formatFullDate(material.createdAt.toDate())
        tvDescription.text = material.description
        tvDownloads.text = material.downloads.toString()
        tvViews.text = material.views.toString()
        tvLikes.text = material.likes.toString()

        // Adiciona as tags
        chipGroupTags.removeAllViews()
        material.categories.forEach { tag ->
            val chip = Chip(this)
            chip.text = tag
            chip.isClickable = true
            chip.isCheckable = false
            chipGroupTags.addView(chip)
        }

        // Atualiza o ícone de favorito no menu
        invalidateOptionsMenu()
    }

    private fun displayComments(comments: List<Comment>) {
        if (comments.isEmpty()) {
            tvNoComments.visibility = View.VISIBLE
            recyclerViewComments.visibility = View.GONE
        } else {
            tvNoComments.visibility = View.GONE
            recyclerViewComments.visibility = View.VISIBLE
            commentAdapter.submitList(comments)
        }
    }

    private fun toggleBookmark() {
        currentMaterial?.let {
            // Alterna o estado de favorito
            val newBookmarkState = !it.bookmarked
            materialViewModel.toggleBookmark(it.id, newBookmarkState)

            // Atualiza o material atual
            currentMaterial = it.copy(bookmarked = newBookmarkState)

            // Atualiza o ícone no menu
            invalidateOptionsMenu()

            // Mostra mensagem de confirmação
            val messageResId = if (newBookmarkState) R.string.material_bookmarked else R.string.material_bookmark_removed
            Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadMaterial() {
        // Simula o download do material
        Toast.makeText(this, R.string.downloading_material, Toast.LENGTH_SHORT).show()

        // implementação real: usar materialViewModel.downloadMaterial(materialId)
    }

    private fun shareMaterial() {
        currentMaterial?.let {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, it.title)

            val shareMessage = getString(
                R.string.share_material_message,
                it.title,
                it.authorName,
                "https://devhive.app/material/${it.id}"
            )

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }

    private fun showAddCommentDialog() {
        val bottomSheet = CreateCommentBottomSheet.newInstance(materialId)
        bottomSheet.show(supportFragmentManager, "CreateCommentBottomSheet")
    }

    override fun onCommentLikeClick(comment: Comment, position: Int) {
        // Simula o utilizador atual
        val currentUserId = "current_user_id" // simulaçao
        commentViewModel.likeComment(comment.id, currentUserId)
    }

    override fun onUserClick(userId: String) {
        // Navega para o perfil do utilizador
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra(UserProfileActivity.EXTRA_USER_ID, userId)
        startActivity(intent)
    }

    private fun getMockMaterial(): Material {
        // Simulamos um material para fins de demonstração
        return Material(
            id = materialId,
            title = "Introdução à Programação em Kotlin",
            description = "Este material apresenta os conceitos básicos da linguagem Kotlin, incluindo sintaxe, estruturas de controle, funções e classes. Ideal para iniciantes que desejam aprender a programar em Kotlin.",
            ownerUid = "user123",
            authorName = "David Reis",
            authorImageUrl = "",
            thumbnailUrl = "",
            contentUrl = "",
            type = "pdf",
            fileSize = 2048,
            categories = listOf("Kotlin", "Programação", "Iniciante"),
            subject = "Programação",
            downloads = 125,
            views = 342,
            likes = 78,
            bookmarked = false,
            createdAt = Timestamp(java.util.Date()),
            updatedAt = Timestamp(java.util.Date()),
            isPublic = true,
            rating = 4.5f,
            reviewCount = 10
        )
    }

    private fun getMockComments(): List<Comment> {
        // Simulamos alguns comentários para fins de demonstração
        return listOf(
            Comment(
                id = "comment1",
                materialId = materialId,
                userId = "user456",
                userName = "Ana Silva",
                userImageUrl = "",
                content = "Excelente material! Muito bem explicado e fácil de entender.",
                likes = 12,
                liked = false,
                parentCommentId = null,
                attachments = emptyList(),
                createdAt = Timestamp(Date(System.currentTimeMillis() - 86400000)), // 1 dia atrás
                updatedAt = Timestamp(Date(System.currentTimeMillis() - 86400000))
            ),
            Comment(
                id = "comment2",
                materialId = materialId,
                userId = "user789",
                userName = "João Pereira",
                userImageUrl = "",
                content = "Ajudou-me muito a entender os conceitos básicos de Kotlin. Recomendo!",
                likes = 8,
                liked = true,
                parentCommentId = null,
                attachments = emptyList(),
                createdAt = Timestamp(Date(System.currentTimeMillis() - 172800000)), // 2 dias atrás
                updatedAt = Timestamp(Date(System.currentTimeMillis() - 172800000))
            )
        )
    }
}
