package ipvc.tp.devhive.presentation.ui.main.chat

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.data.util.SyncStatus
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.model.Message
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModel
import java.util.Date

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

    private val messageAdapter = MessageAdapter()
    private var chatId: String = ""
    private var otherUserId: String = ""
    private var currentChat: Chat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        // Obtém os parâmetros da intent
        chatId = intent.getStringExtra(EXTRA_CHAT_ID) ?: ""
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: ""

        if (chatId.isEmpty() && otherUserId.isEmpty()) {
            finish()
            return
        }

        // Inicializa as views
        toolbar = findViewById(R.id.toolbar)
        ivOtherUserAvatar = findViewById(R.id.iv_other_user_avatar)
        tvOtherUserName = findViewById(R.id.tv_other_user_name)
        tvOtherUserStatus = findViewById(R.id.tv_other_user_status)
        recyclerViewMessages = findViewById(R.id.recycler_view_messages)
        etMessage = findViewById(R.id.et_message)
        btnSend = findViewById(R.id.btn_send)

        // Configura a toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configura o RecyclerView
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewMessages.layoutManager = layoutManager
        recyclerViewMessages.adapter = messageAdapter

        // Carrega os detalhes do chat
        loadChatDetails()

        // Configura o botão de enviar
        btnSend.setOnClickListener {
            sendMessage()
        }
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadChatDetails() {
        // implementação real: usar chatViewModel.getChatById(chatId) ou createChat se não existir
        val mockChat = getMockChat()
        displayChatDetails(mockChat)

        // Carrega as mensagens do chat
        val mockMessages = getMockMessages()
        displayMessages(mockMessages)
    }

    private fun displayChatDetails(chat: Chat) {
        currentChat = chat

        tvOtherUserName.text = chat.otherParticipantName
        tvOtherUserStatus.text = if (chat.otherParticipantOnline) getString(R.string.online) else getString(R.string.offline)

        // Carrega a imagem do outro utilizador
        if (chat.otherParticipantImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(chat.otherParticipantImageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(ivOtherUserAvatar)
        }
    }

    private fun displayMessages(messages: List<Message>) {
        messageAdapter.submitList(messages)
        if (messages.isNotEmpty()) {
            recyclerViewMessages.scrollToPosition(messages.size - 1)
        }
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            return
        }

        // implementação real: usar chatViewModel.sendMessage(chatId, messageText)
        val newMessage = Message(
            id = "msg_" + System.currentTimeMillis(),
            chatId = chatId,
            content = messageText,
            senderUid = "current_user_id",
            createdAt = Timestamp.now(),
            attachments = emptyList(),
            read = false,
            syncStatus = SyncStatus.SYNCED,
            lastSync = Timestamp.now(),
        )

        // Adiciona a mensagem à lista
        val currentMessages = messageAdapter.currentList.toMutableList()
        currentMessages.add(newMessage)
        messageAdapter.submitList(currentMessages)

        // Limpa o campo de texto
        etMessage.text.clear()

        // Rola para a última mensagem
        recyclerViewMessages.scrollToPosition(currentMessages.size - 1)
    }

    private fun getMockChat(): Chat {
        return Chat(
            id = chatId.ifEmpty { "chat_${System.currentTimeMillis()}" },
            participant1Id = "current_user_id",
            participant2Id = otherUserId.ifEmpty { "other_user_id" },
            createdAt = Timestamp(Date(System.currentTimeMillis() - 86400000)),
            updatedAt = Timestamp.now(),
            lastMessageAt = Timestamp(Date(System.currentTimeMillis() - 3600000)),
            lastMessagePreview = "Olá, como vai?",
            messageCount = 5,
            otherParticipantId = otherUserId.ifEmpty { "other_user_id" },
            otherParticipantName = "Ana Silva",
            otherParticipantImageUrl = "",
            otherParticipantOnline = true
        )
    }

    private fun getMockMessages(): List<Message> {
        return listOf(
            Message(
                id = "msg1",
                chatId = chatId,
                content = "Olá, tudo bem?",
                senderUid = otherUserId.ifEmpty { "other_user_id" },
                createdAt = Timestamp(Date(System.currentTimeMillis() - 86400000)),
                attachments = emptyList(),
                read = false,
                syncStatus = SyncStatus.SYNCED,
                lastSync = Timestamp.now(),
            ),
            Message(
                id = "msg2",
                chatId = chatId,
                content = "Olá! Tudo ótimo, e contigo?",
                senderUid = "current_user_id",
                createdAt = Timestamp(Date(System.currentTimeMillis() - 82800000)),
                attachments = emptyList(),
                read = false,
                syncStatus = SyncStatus.SYNCED,
                lastSync = Timestamp.now(),
            ),
            Message(
                id = "msg3",
                chatId = chatId,
                content = "Também estou bem! Preciso de ajuda com um exercício de Kotlin.",
                senderUid = otherUserId.ifEmpty { "other_user_id" },
                createdAt = Timestamp(Date(System.currentTimeMillis() - 7200000)),
                attachments = emptyList(),
                read = false,
                syncStatus = SyncStatus.SYNCED,
                lastSync = Timestamp.now(),
            ),
            Message(
                id = "msg4",
                chatId = chatId,
                content = "Claro, pode enviar!",
                senderUid = "current_user_id",
                createdAt = Timestamp(Date(System.currentTimeMillis() - 3600000)),
                attachments = emptyList(),
                read = false,
                syncStatus = SyncStatus.SYNCED,
                lastSync = Timestamp.now(),
            )
        )
    }
}


