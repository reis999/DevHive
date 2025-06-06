package ipvc.tp.devhive.presentation.ui.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModel
import ipvc.tp.devhive.presentation.viewmodel.profile.ProfileViewModel

class SelectUserActivity : AppCompatActivity(), UserAdapter.OnUserClickListener {

    private val chatViewModel: ChatViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView

    private val userAdapter = UserAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        // Inicializa as views
        toolbar = findViewById(R.id.toolbar)
        searchView = findViewById(R.id.search_view)
        recyclerView = findViewById(R.id.recycler_view)

        // Configura a toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.select_user)

        // Configura o RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter

        // Configura a busca
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // implementação real: filtrar os utilizadores com base no texto da pesquisa
                return true
            }
        })

        // Carrega a lista de utilizadores
        loadUsers()
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
        // implementação real: carregar utilizadores do repositório
        val mockUsers = getMockUsers()
        userAdapter.submitList(mockUsers)
    }

    override fun onUserClick(user: User) {
        // implementação real: criar um chat com o utilizador selecionado
        // e entrar no ecra do chat
        Toast.makeText(this, getString(R.string.creating_chat_with, user.name), Toast.LENGTH_SHORT).show()

        // Simula a criação de um chat e navegação
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_ID, user.id)
        startActivity(intent)
        finish()
    }

    private fun getMockUsers(): List<User> {
        return listOf(
            User(
                id = "user1",
                name = "Ana Silva",
                username = "ana_silva",
                email = "ana.silva@example.com",
                profileImageUrl = "",
                bio = "Estudante de Engenharia de Software",
                institution = "Instituto de Engenharia",
                course = "Engenharia de Software",
                lastLogin = Timestamp.now(),
                createdAt = Timestamp.now(),
                isOnline = true,
                contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                    materials = 2,
                    likes = 4,
                    comments = 1,
                    sessions = 4
                )
            ),
            User(
                id = "user2",
                name = "Carlos Oliveira",
                username = "carlos_oliveira",
                email = "carlos.oliveira@example.com",
                profileImageUrl = "",
                bio = "Professor de Matemática",
                institution = "Universidade XYZ",
                course = "Matemática",
                lastLogin = Timestamp.now(),
                createdAt = Timestamp.now(),
                isOnline = true,
                contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                    materials = 3,
                    likes = 5,
                    comments = 2,
                    sessions = 5
                )
            ),
            User(
                id = "user3",
                name = "Mariana Santos",
                username = "mariana_santos",
                email = "mariana.santos@example.com",
                profileImageUrl = "",
                bio = "Estudante de Ciência da Computação",
                institution = "Universidade XYZ",
                course = "Ciência da Computação",
                lastLogin = Timestamp.now(),
                createdAt = Timestamp.now(),
                isOnline = true,
                contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                    materials = 1,
                    likes = 3,
                    comments = 0,
                    sessions = 3
                )
            )
        )
    }
}
