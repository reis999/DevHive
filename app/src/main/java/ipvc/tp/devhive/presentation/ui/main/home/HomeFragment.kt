package ipvc.tp.devhive.presentation.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.presentation.ui.main.material.AddMaterialActivity
import ipvc.tp.devhive.presentation.ui.main.material.MaterialAdapter
import ipvc.tp.devhive.presentation.ui.main.material.MaterialDetailActivity
import ipvc.tp.devhive.presentation.util.showSnackbar
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialEvent
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel
import android.widget.Toast

@AndroidEntryPoint
class HomeFragment : Fragment(), MaterialAdapter.OnMaterialClickListener {

    private val materialViewModel: MaterialViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private lateinit var materialAdapter: MaterialAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa as views
        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        fabAdd = view.findViewById(R.id.fab_add)
        progressBar = view.findViewById(R.id.progress_bar)
        tvEmpty = view.findViewById(R.id.tv_empty)

        setupRecyclerView()
        setupObservers(view)
        setupListeners()
    }

    private fun setupRecyclerView() {
        materialAdapter = MaterialAdapter(this, null)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = materialAdapter
    }

    private fun setupObservers(view: View) {
        materialViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            updateAdapterWithCurrentUser(user?.id)
        }

        materialViewModel.materials.observe(viewLifecycleOwner) { materials ->
            materialAdapter.submitList(materials)

            if (materials.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }

            swipeRefreshLayout.isRefreshing = false
        }

        // Observa eventos de material
        materialViewModel.materialEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { materialEvent ->
                when (materialEvent) {
                    is MaterialEvent.BookmarkToggled -> {
                        val message = if (materialEvent.bookmarked) {
                            getString(R.string.material_bookmarked)
                        } else {
                            getString(R.string.material_bookmark_removed)
                        }
                        view.showSnackbar(message)
                    }
                    is MaterialEvent.BookmarkFailure -> {
                        view.showSnackbar(materialEvent.message)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateAdapterWithCurrentUser(userId: String?) {
        materialAdapter = MaterialAdapter(this, userId)
        recyclerView.adapter = materialAdapter
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            // TODO: Implementar lógica para recarregar dados
            swipeRefreshLayout.isRefreshing = false
        }

        fabAdd.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), AddMaterialActivity::class.java))
        }
    }

    override fun onMaterialClick(material: Material) {
        val intent = android.content.Intent(requireContext(), MaterialDetailActivity::class.java)
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
        } else {
            Toast.makeText(requireContext(), "É necessário fazer login para marcar favoritos", Toast.LENGTH_SHORT).show()
        }
    }
}