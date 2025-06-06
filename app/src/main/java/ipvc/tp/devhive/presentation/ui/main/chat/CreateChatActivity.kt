package ipvc.tp.devhive.presentation.ui.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.ContributionStats
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModel
import java.util.Date

@AndroidEntryPoint
class CreateChatActivity : AppCompatActivity(), UserAdapter.OnUserClickListener {

    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewUsers: RecyclerView

    private val userAdapter = UserAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_chat)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadUsers()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerViewUsers = findViewById(R.id.recycler_view_users)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.select_user_to_chat)
    }

    private fun setupRecyclerView() {
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        recyclerViewUsers.adapter = userAdapter
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

    private fun loadUsers() {
        // Em uma implementação real, carregaríamos os usuários disponíveis
        // Para fins de demonstração, usamos dados simulados
        val mockUsers = getMockUsers()
        userAdapter.submitList(mockUsers)
    }

    override fun onUserClick(user: User) {
        // Cria um chat direto com o usuário selecionado
        createDirectChat(user)
    }

    private fun createDirectChat(user: User) {
        // Em uma implementação real, usaríamos chatViewModel.createDirectChat()
        // Para fins de demonstração, navegamos diretamente para o chat
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ID, "chat_with_${user.id}")
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_NAME, user.name)
        startActivity(intent)
        finish()
    }

    private fun getMockUsers(): List<User> {
        // Simulamos alguns usuários para fins de demonstração
        return listOf(
            User(
                id = "user1",
                name = "Ana Silva",
                username = "ana.silva",
                email = "ana.silva@email.com",
                bio = "Estudante de Engenharia Informática",
                profileImageUrl = "",
                institution = "IPVC",
                course = "Engenharia Informática",
                createdAt = Timestamp(Date()),
                lastLogin = Timestamp(Date()),
                isOnline = true,
                contributionStats = ContributionStats(
                    materials = 10,
                    comments = 20,
                    likes = 30,
                    sessions = 22
                )
            ),
            User(
                id = "user2",
                name = "João Santos",
                username = "joao.santos",
                email = "joao.santos@email.com",
                bio = "Desenvolvedor Android",
                profileImageUrl = "",
                institution = "IPVC",
                course = "Engenharia Informática",
                createdAt = Timestamp(Date()),
                lastLogin = Timestamp(Date()),
                isOnline = true,
                contributionStats = ContributionStats(
                    materials = 10,
                    comments = 20,
                    likes = 30,
                    sessions = 22
                )
            ),
            User(
                id = "user3",
                name = "Maria Costa",
                username = "maria.costa",
                email = "maria.costa@email.com",
                bio = "Designer UX/UI",
                profileImageUrl = "",
                institution = "IPVC",
                course = "Design Multimédia",
                createdAt = Timestamp(Date()),
                lastLogin = Timestamp(Date()),
                isOnline = true,
                contributionStats = ContributionStats(
                    materials = 10,
                    comments = 20,
                    likes = 30,
                    sessions = 22
                )
            )
        )
    }
}
