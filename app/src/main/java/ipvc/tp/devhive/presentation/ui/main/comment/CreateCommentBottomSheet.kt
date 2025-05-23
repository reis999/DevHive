package ipvc.tp.devhive.presentation.ui.main.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.viewmodel.comment.CommentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreateCommentBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_MATERIAL_ID = "material_id"

        fun newInstance(materialId: String): CreateCommentBottomSheet {
            val fragment = CreateCommentBottomSheet()
            val args = Bundle()
            args.putString(ARG_MATERIAL_ID, materialId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var commentViewModel: CommentViewModel
    private lateinit var etComment: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button

    private var materialId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        materialId = arguments?.getString(ARG_MATERIAL_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_create_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa as views
        etComment = view.findViewById(R.id.et_comment)
        btnSubmit = view.findViewById(R.id.btn_submit)
        btnCancel = view.findViewById(R.id.btn_cancel)

        // Inicializa o ViewModel
        val factory = DevHiveApp.getViewModelFactories().commentViewModelFactory
        commentViewModel = ViewModelProvider(this, factory)[CommentViewModel::class.java]

        // Configura os cliques
        btnSubmit.setOnClickListener {
            submitComment()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        // Foca no campo de texto
        etComment.requestFocus()
    }

    private fun submitComment() {
        val commentText = etComment.text.toString().trim()

        if (commentText.isEmpty()) {
            etComment.error = getString(R.string.error_comment_empty)
            return
        }

        if (commentText.length < 3) {
            etComment.error = getString(R.string.error_comment_too_short)
            return
        }

        // Em uma implementação real, usaríamos commentViewModel.createComment()
        // Para fins de demonstração, apenas mostramos uma mensagem de sucesso
        Toast.makeText(requireContext(), R.string.comment_created_success, Toast.LENGTH_SHORT).show()
        dismiss()
    }
}
