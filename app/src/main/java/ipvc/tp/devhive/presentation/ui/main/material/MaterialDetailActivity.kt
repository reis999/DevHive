package ipvc.tp.devhive.presentation.ui.main.material

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.presentation.ui.main.comment.CommentAdapter
import ipvc.tp.devhive.presentation.ui.main.comment.CreateCommentBottomSheet
import ipvc.tp.devhive.presentation.ui.main.profile.UserProfileActivity
import ipvc.tp.devhive.presentation.util.DateFormatUtils
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthState
import ipvc.tp.devhive.presentation.viewmodel.auth.AuthViewModel
import ipvc.tp.devhive.presentation.viewmodel.comment.CommentEvent
import ipvc.tp.devhive.presentation.viewmodel.comment.CommentViewModel
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialEvent
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel
import java.util.Locale

@AndroidEntryPoint
class MaterialDetailActivity : AppCompatActivity(), CommentAdapter.OnCommentClickListener {

    companion object {
        const val EXTRA_MATERIAL_ID = "extra_material_id"
    }

    private val materialViewModel: MaterialViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

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
    private lateinit var containerFileInfo: CardView
    private lateinit var tvFileType: TextView
    private lateinit var tvFileName: TextView
    private var hasIncrementedViews = false


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
        setupObservers()
        setupClickListeners()
        loadMaterialDetails()
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
        containerFileInfo = findViewById(R.id.container_file_info)
        tvFileType = findViewById(R.id.tv_file_type)
        tvFileName = findViewById(R.id.tv_file_name)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        recyclerViewComments.adapter = commentAdapter
    }

    private fun setupObservers() {
        // Observa o material
        materialViewModel.material.observe(this) { material ->
            material?.let {
                displayMaterialDetails(it)

                if (!hasIncrementedViews) {
                    incrementAndUpdateViews()
                    hasIncrementedViews = true
                }
            }
        }

        // Observa o estado de loading
        materialViewModel.isLoadingMaterial.observe(this) {
            // TODO: Implementar loading UI se necessário
        }

        materialViewModel.isDeletingMaterial.observe(this) {
            // TODO: Mostrar progress durante eliminação se necessário
        }

        // Observa eventos do material
        materialViewModel.materialEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { materialEvent ->
                handleMaterialEvent(materialEvent)
            }
        }

        // Observa os comentários do material
        commentViewModel.getCommentsByMaterial(materialId).observe(this) { comments ->
            displayComments(comments)
        }

        // Observa eventos dos comentários
        commentViewModel.commentEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { commentEvent ->
                handleCommentEvent(commentEvent)
            }
        }

        // Observa o estado de autenticação
        authViewModel.authState.observe(this) { authState ->
            updateUIBasedOnAuthState(authState)
        }
    }

    private fun handleMaterialEvent(event: MaterialEvent) {
        when (event) {
            is MaterialEvent.BookmarkToggled -> {
                val messageResId = if (event.bookmarked)
                    R.string.material_bookmarked
                else
                    R.string.material_bookmark_removed
                Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
                materialViewModel.getMaterialById(materialId)

                val currentUserId = authViewModel.getCurrentUserId()
                currentMaterial?.let { material ->
                    if (currentUserId != null) {
                        val newBookmarkedBy = if (event.bookmarked) {
                            if (currentUserId !in material.bookmarkedBy) {
                                material.bookmarkedBy + currentUserId
                            } else {
                                material.bookmarkedBy
                            }
                        } else {
                            material.bookmarkedBy - currentUserId
                        }

                        currentMaterial = material.copy(
                            bookmarkedBy = newBookmarkedBy,
                            bookmarks = newBookmarkedBy.size
                        )
                        invalidateOptionsMenu()
                    }
                }
            }
            is MaterialEvent.BookmarkFailure -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
            is MaterialEvent.LikeToggled -> {
                val messageResId = if (event.isLiked)
                    R.string.material_liked
                else
                    R.string.material_unliked
                Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
                materialViewModel.getMaterialById(materialId)

                val currentUserId = authViewModel.getCurrentUserId()
                currentMaterial?.let { material ->
                    if (currentUserId != null) {
                        val newLikedBy = if (event.isLiked) {
                            if (currentUserId !in material.likedBy) material.likedBy + currentUserId else material.likedBy
                        } else {
                            material.likedBy - currentUserId
                        }

                        currentMaterial = material.copy(
                            likedBy = newLikedBy,
                            likes = newLikedBy.size
                        )
                        invalidateOptionsMenu()
                    }
                }
            }
            is MaterialEvent.LikeFailure -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
            is MaterialEvent.DownloadSuccess -> {
                startDownload(event.contentUrl)
                Toast.makeText(this, R.string.downloading_material, Toast.LENGTH_SHORT).show()
                materialViewModel.getMaterialById(materialId)
            }
            is MaterialEvent.DownloadFailure -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
            is MaterialEvent.DeleteSuccess -> {
                Toast.makeText(this, "Material eliminado com sucesso!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            is MaterialEvent.DeleteFailure -> {
                Toast.makeText(this, "Erro ao eliminar: ${event.message}", Toast.LENGTH_LONG).show()
            }
            is MaterialEvent.ShowMessage -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }
    }

    private fun handleCommentEvent(event: CommentEvent) {
        when (event) {
            is CommentEvent.CreateSuccess -> {
                Toast.makeText(this, R.string.comment_created_success, Toast.LENGTH_SHORT).show()
            }
            is CommentEvent.CreateFailure -> {
                Toast.makeText(this, "Erro ao criar comentário: ${event.message}", Toast.LENGTH_SHORT).show()
            }
            is CommentEvent.LikeSuccess -> {
                Toast.makeText(this, R.string.comment_liked, Toast.LENGTH_SHORT).show()
            }
            is CommentEvent.LikeFailure -> {
                Toast.makeText(this, "Erro ao curtir comentário: ${event.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
                intent.putExtra(UserProfileActivity.EXTRA_CURRENT_USER_ID, authViewModel.getCurrentUserId())
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.material_detail_menu, menu)

        currentMaterial?.let { material ->
            val currentUserId = authViewModel.getCurrentUserId()

            val bookmarkItem = menu.findItem(R.id.action_bookmark)
            val isBookmarked = currentUserId != null && currentUserId in material.bookmarkedBy
            val bookmarkIconRes = if (isBookmarked) {
                R.drawable.ic_bookmark
            } else {
                R.drawable.ic_bookmark_border
            }
            bookmarkItem.setIcon(bookmarkIconRes)

            val likeItem = menu.findItem(R.id.action_like)
            val isLiked = currentUserId != null && currentUserId in material.likedBy

            val likeIconRes = if (isLiked) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_outline
            }
            likeItem.setIcon(likeIconRes)

            val deleteItem = menu.findItem(R.id.action_delete)
            val isOwner = currentUserId != null && currentUserId == material.ownerUid
            deleteItem.isVisible = isOwner
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_like -> {
                toggleMaterialLike()
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
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadMaterialDetails() {
        materialViewModel.getMaterialById(materialId)
    }

    private fun displayMaterialDetails(material: Material) {
        currentMaterial = material

        collapsingToolbar.title = material.title

        if (material.thumbnailUrl.isNotEmpty()) {
            Glide.with(this)
                .load(material.thumbnailUrl)
                .placeholder(R.drawable.material_placeholder)
                .error(R.drawable.material_placeholder)
                .into(ivMaterialCover)
        }

        if (material.ownerImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(material.ownerImageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(ivAuthorAvatar)
        }

        tvAuthorName.text = material.ownerName
        tvUploadDate.text = DateFormatUtils.formatFullDate(material.createdAt.toDate())
        tvDescription.text = material.description
        tvDownloads.text = String.format(Locale.getDefault(), "%,d", material.downloads)
        tvViews.text = String.format(Locale.getDefault(), "%,d", material.views)
        tvLikes.text = String.format(Locale.getDefault(), "%,d", material.likedBy.size)

        // Adiciona as tags
        chipGroupTags.removeAllViews()
        material.categories.forEach { tag ->
            val chip = Chip(this)
            chip.text = tag
            chip.isClickable = true
            chip.isCheckable = false
            chipGroupTags.addView(chip)
        }

        displayFileInfo(material)

        invalidateOptionsMenu()
    }

    private fun displayFileInfo(material: Material) {
        Log.d("MaterialDetailActivity", "Displaying file info for material: ${material.type}")
        val typeText = "Tipo: ${material.type}"
        tvFileType.text = typeText

        val fileName = extractFileNameFromUrl(material.contentUrl)
        tvFileName.text = fileName.ifEmpty {
            "${material.title}.${getFileExtension(material.type)}"
        }

        val fileSizeText = if (material.fileSize > 0) {
            " • ${formatFileSize(material.fileSize)}"
        } else {
            ""
        }

        val fullTypeText = typeText + fileSizeText
        tvFileType.text = fullTypeText

        containerFileInfo.visibility = if (material.contentUrl.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @SuppressLint("UseKtx")
    private fun extractFileNameFromUrl(url: String): String {
        return try {
            if (url.isEmpty()) return ""

            val uri = url.toUri()
            val pathSegments = uri.pathSegments

            val lastSegment = pathSegments.lastOrNull { it.contains(".") } ?: pathSegments.lastOrNull() ?: ""

            val decodedSegment = Uri.decode(lastSegment)

            val fileName = decodedSegment.substringAfterLast("/")

            fileName
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", sizeInBytes / 1024.0)
            sizeInBytes < 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", sizeInBytes / (1024.0 * 1024.0))
            else -> String.format(Locale.getDefault(), "%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    private fun getFileExtension(type: String): String {
        return when (type.lowercase()) {
            "pdf", getString(R.string.type_pdf) -> "pdf"
            "video", getString(R.string.type_video) -> "mp4"
            "audio", getString(R.string.type_audio) -> "mp3"
            "image", getString(R.string.type_image) -> "jpg"
            "document", getString(R.string.type_document) -> "docx"
            "presentation", getString(R.string.type_presentation) -> "pptx"
            "spreadsheet", getString(R.string.type_spreadsheet) -> "xlsx"
            "code", getString(R.string.type_code) -> "kt"
            else -> "file"
        }
    }

    private fun incrementAndUpdateViews() {
        currentMaterial?.let { material ->
            // Atualiza a UI imediatamente
            val newViewCount = material.views + 1
            tvViews.text = String.format(Locale.getDefault(), "%,d", newViewCount)

            // Atualiza o material atual
            currentMaterial = material.copy(views = newViewCount)

            // Incrementa no servidor/banco de dados
            materialViewModel.incrementViews(materialId)
        }
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

    private fun showDeleteConfirmationDialog() {
        currentMaterial?.let {
            AlertDialog.Builder(this)
                .setTitle("Eliminar Material")
                .setMessage("Tens a certeza que desejas eliminar este material? Esta ação não pode ser desfeita.")
                .setPositiveButton("Eliminar") { _, _ ->
                    deleteMaterial()
                }
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_warning)
                .show()
        }
    }

    private fun deleteMaterial() {
        materialViewModel.deleteMaterial(materialId)
    }

    private fun toggleMaterialLike() {
        if (authViewModel.isAuthenticated()) {
            currentMaterial?.let { material ->
                val userId = authViewModel.getCurrentUserId()
                if (userId != null) {
                    val isCurrentlyLiked = userId in material.likedBy
                    val newLikeState = !isCurrentlyLiked
                    materialViewModel.toggleMaterialLike(material.id, userId, newLikeState)
                }
            }
        } else {
            Toast.makeText(this, R.string.login_required_to_like, Toast.LENGTH_SHORT).show()
            // navigateToLogin()
        }
    }

    private fun toggleBookmark() {
        if (authViewModel.isAuthenticated()) {
            currentMaterial?.let { material ->
                val userId = authViewModel.getCurrentUserId()
                if (userId != null) {
                    val isCurrentlyBookmarked = userId in material.bookmarkedBy
                    val newBookmarkState = !isCurrentlyBookmarked

                    materialViewModel.toggleBookmark(
                        materialId = material.id,
                        userId = userId,
                        isBookmarked = newBookmarkState
                    )
                }
            }
        } else {
            Toast.makeText(this, "É necessário fazer login para marcar favoritos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadMaterial() {
        materialViewModel.downloadMaterial(materialId)
    }

    private fun startDownload(contentUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, contentUrl.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir download: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareMaterial() {
        currentMaterial?.let {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, it.title)

            val shareMessage = getString(
                R.string.share_material_message,
                it.title,
                it.ownerName,
                "https://devhive.app/material/${it.id}"
            )

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }

    private fun updateUIBasedOnAuthState(authState: AuthState) {
        when (authState) {
            is AuthState.Authenticated -> {
                fabAddComment.isEnabled = true
                fabAddComment.alpha = 1.0f
            }
            is AuthState.Unauthenticated -> {
                fabAddComment.isEnabled = true
                fabAddComment.alpha = 0.7f
            }
            is AuthState.Loading -> {
                fabAddComment.isEnabled = false
                fabAddComment.alpha = 0.5f
            }
            is AuthState.Error -> {
                fabAddComment.isEnabled = false
                fabAddComment.alpha = 0.5f
            }
        }
    }

    private fun showAddCommentDialog() {
        val bottomSheet = CreateCommentBottomSheet.newInstance(materialId)
        bottomSheet.show(supportFragmentManager, "CreateCommentBottomSheet")
    }

    override fun onCommentLikeClick(comment: Comment, position: Int) {
        if (authViewModel.isAuthenticated()) {
            val userId = authViewModel.getCurrentUserId()
            if (userId != null) {
                commentViewModel.likeComment(comment.id, userId)
            }
        } else {
            Toast.makeText(this, R.string.login_required_to_like, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUserClick(userId: String) {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra(UserProfileActivity.EXTRA_USER_ID, userId)
        startActivity(intent)
    }
}