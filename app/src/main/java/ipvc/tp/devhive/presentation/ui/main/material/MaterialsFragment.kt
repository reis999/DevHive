package ipvc.tp.devhive.presentation.ui.main.material

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.presentation.util.showSnackbar
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialEvent
import ipvc.tp.devhive.presentation.viewmodel.material.MaterialViewModel

class MaterialsFragment : Fragment(), MaterialAdapter.OnMaterialClickListener {

    private val materialViewModel: MaterialViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private val materialAdapter = MaterialAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragmento
        return inflater.inflate(R.layout.fragment_materials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa as views
        recyclerView = view.findViewById(R.id.recycler_view)
        searchView = view.findViewById(R.id.search_view)
        fabAdd = view.findViewById(R.id.fab_add)
        progressBar = view.findViewById(R.id.progress_bar)
        tvEmpty = view.findViewById(R.id.tv_empty)

        // Configura o RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = materialAdapter

        // Observa a lista de materiais
        materialViewModel.materials.observe(viewLifecycleOwner) { materials ->
            materialAdapter.submitList(materials)

            // Mostra mensagem de lista vazia se não houver materiais
            if (materials.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }

            // Esconde o loading
            progressBar.visibility = View.GONE
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
                    else -> {} // Ignora outros eventos
                }
            }
        }

        // Configura o SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Implementação da pesquisa
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtra a lista à medida que o utilizador escreve
                return true
            }
        })

        // Configura o FAB para adicionar um novo material
        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddMaterialActivity::class.java))
        }
    }

    override fun onMaterialClick(material: Material) {
        // Abre o ecra de detalhes do material
        val intent = Intent(requireContext(), MaterialDetailActivity::class.java)
        intent.putExtra(MaterialDetailActivity.EXTRA_MATERIAL_ID, material.id)
        startActivity(intent)
    }

    override fun onBookmarkClick(material: Material, position: Int) {
        // Alterna o estado de favorito do material
        materialViewModel.toggleBookmark(material.id, !material.bookmarked)
    }
}
