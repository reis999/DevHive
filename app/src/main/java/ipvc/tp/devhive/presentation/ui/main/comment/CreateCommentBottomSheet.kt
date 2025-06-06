package ipvc.tp.devhive.presentation.ui.main.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.viewmodel.comment.CommentViewModel

class CreateCommentBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_MATERIAL_ID = "material_id"
        private const val ARG_PARENT_COMMENT_ID = "parent_comment_id"

        fun newInstance(materialId: String, parentCommentId: String? = null): CreateCommentBottomSheet {
            val fragment = CreateCommentBottomSheet()
            val args = Bundle()
            args.putString(ARG_MATERIAL_ID, materialId)
            parentCommentId?.let { args.putString(ARG_PARENT_COMMENT_ID, it) }
            fragment.arguments = args
            return fragment
        }
    }

    private val commentViewModel: CommentViewModel by viewModels()
    private lateinit var etComment: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button

    private var materialId: String = ""
    private var parentCommentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            materialId = it.getString(ARG_MATERIAL_ID) ?: ""
            parentCommentId = it.getString(ARG_PARENT_COMMENT_ID)
        }
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

        initializeViews(view)
        setupClickListeners()
        observeEvents()

        // Foca no campo de texto
        etComment.requestFocus()
    }

    private fun initializeViews(view: View) {
        etComment = view.findViewById(R.id.et_comment)
        btnSubmit = view.findViewById(R.id.btn_submit)
        btnCancel = view.findViewById(R.id.btn_cancel)
    }

    private fun setupClickListeners() {
        btnSubmit.setOnClickListener {
            submitComment()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun observeEvents() {
        commentViewModel.commentEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { commentEvent ->
                when (commentEvent) {
                    is ipvc.tp.devhive.presentation.viewmodel.comment.CommentEvent.CreateSuccess -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.comment_created_success,
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }
                    is ipvc.tp.devhive.presentation.viewmodel.comment.CommentEvent.CreateFailure -> {
                        Toast.makeText(
                            requireContext(),
                            commentEvent.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        btnSubmit.isEnabled = true
                    }
                    else -> {}
                }
            }
        }
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

        // Desabilita o botão para evitar múltiplos cliques
        btnSubmit.isEnabled = false

        // Simula o ID do usuário atual (em uma implementação real, viria do sistema de autenticação)
        val currentUserId = "current_user_id"

        commentViewModel.createComment(
            materialId = materialId,
            userUid = currentUserId,
            content = commentText,
            parentCommentId = parentCommentId
        )
    }
}
