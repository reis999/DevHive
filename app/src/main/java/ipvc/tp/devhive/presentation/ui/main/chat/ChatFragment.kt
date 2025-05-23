package ipvc.tp.devhive.presentation.ui.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatFragment : Fragment(), ChatAdapter.OnChatClickListener {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private val chatAdapter = ChatAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragmento
        return inflater.inflate(R.layout.fragment_chat, container, false)
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
        recyclerView.adapter = chatAdapter

        // Inicializa o ViewModel
        val factory = DevHiveApp.getViewModelFactories().chatViewModelFactory
        chatViewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        // Configura o FAB para criar um novo chat
        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), CreateChatActivity::class.java))
        }

        // Como não temos implementação completa, simulamos uma lista vazia
        tvEmpty.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    override fun onChatClick(chat: Chat) {
        // Abre a tela de chat
        val intent = Intent(requireContext(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ID, chat.id)
        startActivity(intent)
    }
}
