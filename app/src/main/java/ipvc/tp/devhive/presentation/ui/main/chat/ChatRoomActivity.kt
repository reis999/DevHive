package ipvc.tp.devhive.presentation.ui.main.chat

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.ui.main.chat.adapters.MessageAdapter
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatEvent
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModel

@AndroidEntryPoint
class ChatRoomActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHAT_ID = "extra_chat_id"
        const val EXTRA_OTHER_USER_ID = "extra_other_user_id"
        const val EXTRA_CHAT_NAME = "extra_other_user_name"
    }

    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var ivOtherUserAvatar: CircleImageView
    private lateinit var tvOtherUserName: TextView
    private lateinit var tvOtherUserStatus: TextView
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var progressBarMessages: ProgressBar

    private lateinit var messageAdapter: MessageAdapter
    private var currentChatId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        currentChatId = intent.getStringExtra(EXTRA_CHAT_ID)
        val initialOtherUserName = intent.getStringExtra(EXTRA_CHAT_NAME)
        // val otherUserIdFromIntent = intent.getStringExtra(EXTRA_OTHER_USER_ID) // Se precisar

        if (currentChatId.isNullOrEmpty()) {
            Toast.makeText(this, "ID do Chat inválido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initializeViews()
        setupToolbar(initialOtherUserName) // Passa o nome inicial se disponível
        setupRecyclerView() // Adapter será configurado quando tivermos currentUserId

        observeViewModel()

        btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        ivOtherUserAvatar = findViewById(R.id.iv_other_user_avatar)
        tvOtherUserName = findViewById(R.id.tv_other_user_name)
        tvOtherUserStatus = findViewById(R.id.tv_other_user_status)
        recyclerViewMessages = findViewById(R.id.recycler_view_messages)
        etMessage = findViewById(R.id.et_message)
        btnSend = findViewById(R.id.btn_send)
        progressBarMessages = findViewById(R.id.progress_bar_messages_chat_room)
    }

    private fun setupToolbar(initialName: String?) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if (!initialName.isNullOrEmpty()) {
            tvOtherUserName.text = initialName
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewMessages.layoutManager = layoutManager
    }

    private fun initializeAdapterIfNeeded(userId: String) {
        if (!::messageAdapter.isInitialized || currentUserId != userId) {
            currentUserId = userId
            messageAdapter = MessageAdapter(userId)
            recyclerViewMessages.adapter = messageAdapter
        }
    }

    private fun observeViewModel() {
        chatViewModel.currentUser.observe(this) { user ->
            if (user != null) {
                initializeAdapterIfNeeded(user.id) // Garante que o adapter está pronto
                currentChatId?.let {
                    chatViewModel.loadChatDetailsAndMessages(it)
                    chatViewModel.listenForMessages(it) // Para atualizações em tempo real
                }
            } else {
                Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        chatViewModel.selectedChat.observe(this) { chat ->
            // chat pode ser nulo se não for encontrado ou durante o carregamento
            // Os detalhes do outro usuário serão atualizados por _otherUserDetails
        }

        chatViewModel.otherUserDetails.observe(this) { otherUser ->
            otherUser?.let { displayOtherUserDetails(it) }
        }

        chatViewModel.chatMessages.observe(this) { messages ->
            if (::messageAdapter.isInitialized) {
                messageAdapter.submitList(messages) {
                    // Rola para a última mensagem somente se a lista não estiver vazia
                    // e, idealmente, apenas se uma nova mensagem foi adicionada pelo usuário atual
                    // ou se é o carregamento inicial.
                    if (messages.isNotEmpty()) {
                        recyclerViewMessages.post { // Use post para garantir que o layout esteja completo
                            recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
                        }
                    }
                }
            }
        }

        chatViewModel.isLoading.observe(this) { isLoading ->
            // Este isLoading é para carregar os detalhes do chat e o primeiro lote de mensagens
            progressBarMessages.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                recyclerViewMessages.visibility = View.GONE
            } else {
                recyclerViewMessages.visibility = View.VISIBLE
            }
        }

        chatViewModel.chatEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { chatEvent ->
                when (chatEvent) {
                    is ChatEvent.SendMessageSuccess -> {
                        etMessage.text.clear()
                        // A lista será atualizada pelo chatMessages.observe
                        // O scroll já é tratado lá.
                    }
                    is ChatEvent.SendMessageFailure -> {
                        Toast.makeText(this, chatEvent.message, Toast.LENGTH_SHORT).show()
                    }
                    is ChatEvent.Error -> {
                        Toast.makeText(this, chatEvent.message, Toast.LENGTH_LONG).show()
                        // Você pode querer finalizar a activity se for um erro crítico (ex: chat não encontrado)
                        if (chatEvent.message.contains("Chat não encontrado")) {
                            // finish()
                        }
                    }
                    else -> { /* Não tratar outros eventos aqui */ }
                }
            }
        }
    }

    private fun displayOtherUserDetails(otherUser: User) {
        tvOtherUserName.text = otherUser.name // Ou username se preferir
        tvOtherUserStatus.text = if (otherUser.isOnline == true) getString(R.string.online) else getString(R.string.offline) // Assumindo que User tem isOnline

        if (!otherUser.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(otherUser.profileImageUrl)
                .placeholder(R.drawable.profile_placeholder) // Use o placeholder correto
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(ivOtherUserAvatar)
        } else {
            ivOtherUserAvatar.setImageResource(R.drawable.profile_placeholder)
        }
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            // Permitir enviar apenas anexos no futuro, se necessário.
            // Por enquanto, uma mensagem de texto é necessária ou um anexo.
            return
        }

        currentChatId?.let { chatId ->
            // O currentUserId já deve estar definido via chatViewModel.currentUser
            chatViewModel.sendMessage(
                chatId = chatId,
                content = messageText
                // attachments = listaDeAnexos // Adicionar lógica de anexos depois
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Volta para a tela anterior
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}