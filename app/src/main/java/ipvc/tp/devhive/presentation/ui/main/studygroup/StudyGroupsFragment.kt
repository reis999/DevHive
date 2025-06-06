package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.content.Intent
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
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModel

class StudyGroupsFragment : Fragment() {

    private val studyGroupViewModel: StudyGroupViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private val studyGroupAdapter = StudyGroupAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragmento
        return inflater.inflate(R.layout.fragment_study_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa as views
        recyclerView = view.findViewById(R.id.recycler_view)
        fabAdd = view.findViewById(R.id.fab_add)
        progressBar = view.findViewById(R.id.progress_bar)
        tvEmpty = view.findViewById(R.id.tv_empty)

        // Configura o RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = studyGroupAdapter


        // Configura o FAB para criar um novo grupo de estudo
        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), CreateStudyGroupActivity::class.java))
        }

        // Como o ViewModel está vazio, simulamos uma lista vazia
        tvEmpty.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
    }
}
