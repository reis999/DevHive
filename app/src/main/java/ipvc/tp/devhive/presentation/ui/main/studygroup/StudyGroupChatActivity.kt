package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import de.hdodenhof.circleimageview.CircleImageView
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.presentation.viewmodel.studygroup.StudyGroupViewModel
import java.util.Date

class StudyGroupChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDY_GROUP_ID = "extra_study_group_id"
    }

    private val studyGroupViewModel: StudyGroupViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var ivGroupImage: CircleImageView
    private lateinit var tvGroupName: TextView
    private lateinit var tvMemberCount: TextView
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton

    private val groupMessageAdapter = GroupMessageAdapter("current_user_id")
    private var studyGroupId: String = ""
    private var currentStudyGroup: StudyGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_group_chat)

        // Obtém o ID do grupo da intent
        studyGroupId = intent.getStringExtra(EXTRA_STUDY_GROUP_ID) ?: ""
        if (studyGroupId.isEmpty()) {
            finish()
            return
        }

        // Inicializa as views
        toolbar = findViewById(R.id.toolbar)
        ivGroupImage = findViewById(R.id.iv_group_image)
        tvGroupName = findViewById(R.id.tv_group_name)
        tvMemberCount = findViewById(R.id.tv_member_count)
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
        recyclerViewMessages.adapter = groupMessageAdapter

        // Carrega os detalhes do grupo
        loadStudyGroupDetails()

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

    private fun loadStudyGroupDetails() {
        // implementação real: usar studyGroupViewModel.getStudyGroupById(studyGroupId)
        val mockStudyGroup = getMockStudyGroup()
        displayStudyGroupDetails(mockStudyGroup)

        // Carrega as mensagens do grupo
        val mockMessages = getMockGroupMessages()
        displayMessages(mockMessages)
    }

    private fun displayStudyGroupDetails(studyGroup: StudyGroup) {
        currentStudyGroup = studyGroup

        tvGroupName.text = studyGroup.name
        tvMemberCount.text = getString(R.string.member_count, studyGroup.members.size)

        // Carrega a imagem do grupo
        if (studyGroup.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(studyGroup.imageUrl)
                .placeholder(R.drawable.placeholder_group)
                .error(R.drawable.placeholder_group)
                .circleCrop()
                .into(ivGroupImage)
        }
    }

    private fun displayMessages(messages: List<GroupMessage>) {
        groupMessageAdapter.submitList(messages)
        if (messages.isNotEmpty()) {
            recyclerViewMessages.scrollToPosition(messages.size - 1)
        }
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            return
        }

        // implementação real: usar studyGroupViewModel.sendGroupMessage()
        val newMessage = GroupMessage(
            id = "msg_" + System.currentTimeMillis(),
            studyGroupId = studyGroupId,
            content = messageText,
            senderUid = "current_user_id",
            senderName = "Você",
            senderImageUrl = "",
            createdAt = Timestamp(Date()),
            attachments = emptyList()
        )

        // Adiciona a mensagem à lista
        val currentMessages = groupMessageAdapter.currentList.toMutableList()
        currentMessages.add(newMessage)
        groupMessageAdapter.submitList(currentMessages)

        // Limpa o campo de texto
        etMessage.text.clear()

        // Rola para a última mensagem
        recyclerViewMessages.scrollToPosition(currentMessages.size - 1)
    }

    private fun getMockStudyGroup(): StudyGroup {
        return StudyGroup(
            id = studyGroupId,
            name = "Grupo de Programação Java",
            description = "Grupo dedicado ao estudo de Java",
            createdBy = "creator_id",
            createdAt = Timestamp(Date(System.currentTimeMillis() - 86400000 * 7)),
            updatedAt = Timestamp(Date()),
            imageUrl = "",
            members = listOf("current_user_id", "user2", "user3", "user4"),
            admins = listOf("creator_id"),
            categories = listOf("Programação", "Java"),
            isPrivate = false,
            joinCode = "",
            maxMembers = 50,
            lastMessageAt = Timestamp(Date(System.currentTimeMillis() - 3600000)),
            lastMessagePreview = "Alguém pode ajudar com este exercício?",
            messageCount = 15
        )
    }

    private fun getMockGroupMessages(): List<GroupMessage> {
        return listOf(
            GroupMessage(
                id = "msg1",
                studyGroupId = studyGroupId,
                content = "Olá pessoal! Bem-vindos ao grupo de Java!",
                senderUid = "creator_id",
                senderName = "João Silva",
                senderImageUrl = "",
                createdAt = Timestamp(Date(System.currentTimeMillis() - 86400000)),
                attachments = emptyList()
            ),
            GroupMessage(
                id = "msg2",
                studyGroupId = studyGroupId,
                content = "Obrigado! Estou animado para aprender!",
                senderUid = "user2",
                senderName = "Maria Santos",
                senderImageUrl = "",
                createdAt = Timestamp(Date(System.currentTimeMillis() - 82800000)),
                attachments = emptyList()
            ),
            GroupMessage(
                id = "msg3",
                studyGroupId = studyGroupId,
                content = "Alguém pode me ajudar com este exercício de herança?",
                senderUid = "user3",
                senderName = "Pedro Costa",
                senderImageUrl = "",
                createdAt = Timestamp(Date(System.currentTimeMillis() - 7200000)),
                attachments = emptyList()
            )
        )
    }
}
