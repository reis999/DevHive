package ipvc.tp.devhive.presentation.ui.main.material

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.presentation.util.showSnackbar
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialEvent
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel

@AndroidEntryPoint
class MaterialsFragment : Fragment(), MaterialAdapter.OnMaterialClickListener {

    private val materialViewModel: MaterialViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var chipGroupSubjects: ChipGroup
    private lateinit var ivClearFilters: ImageView

    private lateinit var materialAdapter: MaterialAdapter

    private val addMaterialLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Material publicado com sucesso!", Toast.LENGTH_SHORT).show()
            recyclerView.scrollToPosition(0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_materials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupObservers(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        searchView = view.findViewById(R.id.search_view)
        fabAdd = view.findViewById(R.id.fab_add)
        progressBar = view.findViewById(R.id.progress_bar)
        tvEmpty = view.findViewById(R.id.tv_empty)
        chipGroupSubjects = view.findViewById(R.id.chip_group_subjects)
        ivClearFilters = view.findViewById(R.id.iv_clear_filters)
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

        // Observa materiais (com MediatorLiveData)
        materialViewModel.materials.observe(viewLifecycleOwner) { materials ->
            materialAdapter.submitList(materials)

            if (materials.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }

            progressBar.visibility = View.GONE
        }

        // Observa os subjects disponíveis
        materialViewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            setupSubjectChips(subjects)
        }

        // Observa eventos de material
        materialViewModel.materialEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { materialEvent ->
                when (materialEvent) {
                    is MaterialEvent.BookmarkToggled -> {
                        val message = if (materialEvent.bookmarked) {
                            "Material adicionado aos favoritos"
                        } else {
                            "Material removido dos favoritos"
                        }
                        view.showSnackbar(message)
                    }
                    is MaterialEvent.BookmarkFailure -> {
                        view.showSnackbar(materialEvent.message)
                    }
                    is MaterialEvent.CreateSuccess -> {
                        view.showSnackbar("Material criado com sucesso!")
                        recyclerView.scrollToPosition(0)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupSubjectChips(subjects: List<String>) {
        chipGroupSubjects.removeAllViews()

        // Chip para "Todos"
        val allChip = Chip(requireContext()).apply {
            text = "Todos"
            isCheckable = true
            isChecked = materialViewModel.getCurrentSubjectFilter().isNullOrBlank()
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    clearSubjectFilter()
                }
            }
        }
        chipGroupSubjects.addView(allChip)

        // Chips para cada subject
        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject
                isCheckable = true
                isChecked = materialViewModel.getCurrentSubjectFilter() == subject
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        materialViewModel.filterBySubject(subject)
                        updateClearFiltersVisibility()
                    }
                }
            }
            chipGroupSubjects.addView(chip)
        }
    }

    private fun clearSubjectFilter() {
        materialViewModel.filterBySubject(null)
        // Atualiza os chips
        for (i in 0 until chipGroupSubjects.childCount) {
            val chip = chipGroupSubjects.getChildAt(i) as Chip
            chip.isChecked = i == 0 // Só o primeiro chip (Todos) fica selecionado
        }
        updateClearFiltersVisibility()
    }

    private fun updateClearFiltersVisibility() {
        val hasActiveFilters = !materialViewModel.getCurrentSearchQuery().isNullOrBlank() ||
                !materialViewModel.getCurrentSubjectFilter().isNullOrBlank()
        ivClearFilters.visibility = if (hasActiveFilters) View.VISIBLE else View.GONE
    }

    private fun updateAdapterWithCurrentUser(userId: String?) {
        materialAdapter = MaterialAdapter(this, userId)
        recyclerView.adapter = materialAdapter
    }

    private fun setupListeners() {
        // IMPLEMENTAÇÃO REAL DA PESQUISA
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    materialViewModel.searchMaterials(it)
                    updateClearFiltersVisibility()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    materialViewModel.searchMaterials("")
                    updateClearFiltersVisibility()
                } else if (newText.length >= 2) {
                    // Pesquisa em tempo real quando tem 2+ caracteres
                    materialViewModel.searchMaterials(newText)
                    updateClearFiltersVisibility()
                }
                return true
            }
        })

        fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddMaterialActivity::class.java)
            addMaterialLauncher.launch(intent)
        }

        // Botão para limpar filtros
        ivClearFilters.setOnClickListener {
            clearAllFilters()
        }
    }

    private fun clearAllFilters() {
        // Limpa a pesquisa
        searchView.setQuery("", false)
        searchView.clearFocus()

        // Limpa filtros no ViewModel
        materialViewModel.clearFilters()

        // Atualiza os chips
        clearSubjectFilter()

        updateClearFiltersVisibility()
    }

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
        } else {
            Toast.makeText(requireContext(), "É necessário fazer login para marcar favoritos", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshMaterials() {
        recyclerView.scrollToPosition(0)
    }
}