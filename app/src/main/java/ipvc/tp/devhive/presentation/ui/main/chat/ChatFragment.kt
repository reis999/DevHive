package ipvc.tp.devhive.presentation.ui.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.ui.main.chat.adapters.ChatAdapter
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatEvent
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModel

@AndroidEntryPoint
class ChatFragment : Fragment(), ChatAdapter.OnChatClickListener {

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private lateinit var chatAdapter: ChatAdapter
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        fabAdd = view.findViewById(R.id.fab_add)
        progressBar = view.findViewById(R.id.progress_bar)
        tvEmpty = view.findViewById(R.id.tv_empty)

        setupRecyclerView()

        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), SelectUserActivity::class.java))
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        chatViewModel.currentUser.value?.id?.let { userId ->
            chatViewModel.loadUserChats(userId)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initializeAdapterIfNeeded(user: User) {
        if (!::chatAdapter.isInitialized || currentUserId != user.id) {
            currentUserId = user.id
            chatAdapter = ChatAdapter(this, user)
            recyclerView.adapter = chatAdapter
        }
    }


    private fun observeViewModel() {
        chatViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                initializeAdapterIfNeeded(user)
                chatViewModel.loadUserChats(user.id)
            } else {
                tvEmpty.text = getString(R.string.user_not_auth)
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                progressBar.visibility = View.GONE
                if (::chatAdapter.isInitialized) {
                    chatAdapter.submitList(emptyList())
                }
            }
        }

        chatViewModel.userChats.observe(viewLifecycleOwner) { chats ->
            if (::chatAdapter.isInitialized) {
                chatAdapter.submitList(chats)
                if (chats.isEmpty()) {
                    tvEmpty.text = getString(R.string.no_chats_found)
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }

        chatViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                tvEmpty.visibility = View.GONE
                recyclerView.visibility = View.GONE
            }
        }

        chatViewModel.chatEvent.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { chatEvent ->
                when (chatEvent) {
                    is ChatEvent.Error -> {
                        Toast.makeText(requireContext(), chatEvent.message, Toast.LENGTH_LONG).show()
                    }

                    else -> { /* Não tratar outros eventos aqui */ }
                }
            }
        }
    }

    override fun onChatClick(chat: Chat) {
        val intent = Intent(requireContext(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ID, chat.id)
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_NAME, chat.otherParticipantName)
        intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_ID, chat.otherParticipantId)
        startActivity(intent)
    }
}
