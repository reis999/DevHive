package ipvc.tp.devhive.presentation.ui.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.ui.main.chat.adapters.UserAdapter
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatEvent
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModel

@AndroidEntryPoint
class SelectUserActivity : AppCompatActivity(), UserAdapter.OnUserClickListener {


    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoUsers: TextView

    private val userAdapter = UserAdapter(this)
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        toolbar = findViewById(R.id.toolbar)
        searchView = findViewById(R.id.search_view)
        recyclerView = findViewById(R.id.recycler_view)
        progressBar = findViewById(R.id.progress_bar_select_user)
        tvNoUsers = findViewById(R.id.tv_no_users_select_user)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.select_user_to_chat)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter

        observeViewModels()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                chatViewModel.searchUsers(query ?: "")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                chatViewModel.searchUsers(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModels() {
        chatViewModel.currentUser.observe(this) { user ->
            currentUserId = user?.id
            if (user != null) {
                currentUserId = user.id
                chatViewModel.loadAllUsersForSelection(excludeUserId = user.id)
            } else {
                Toast.makeText(this, "Utilizador não autenticado. Verifique o login.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        chatViewModel.searchedUsers.observe(this) { users ->
            userAdapter.submitList(users)
            tvNoUsers.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (users.isEmpty()) View.GONE else View.VISIBLE
        }

        chatViewModel.isUserSearchLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                tvNoUsers.visibility = View.GONE
                recyclerView.visibility = View.GONE
            }
        }

        chatViewModel.chatEvent.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { chatEvent ->
                when (chatEvent) {
                    is ChatEvent.CreateSuccess -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, getString(R.string.chat_created_successfully), Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ChatRoomActivity::class.java)

                        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ID, chatEvent.chat.id)
                        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_NAME, chatEvent.chat.otherParticipantName)
                        intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_ID, chatEvent.chat.otherParticipantId)

                        startActivity(intent)
                        finish()
                    }
                    is ChatEvent.CreateFailure -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, chatEvent.message, Toast.LENGTH_LONG).show()
                    }
                    is ChatEvent.Error -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, chatEvent.message, Toast.LENGTH_LONG).show()
                    }
                    else -> { /* Não tratar outros eventos aqui */ }
                }
            }
        }
        chatViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun onUserClick(user: User) {
        val cUserId = currentUserId
        if (cUserId == null) {
            Toast.makeText(this, "Não foi possível identificar o usuário atual.", Toast.LENGTH_SHORT).show()
            return
        }
        if (user.id == cUserId) {
            Toast.makeText(this, "Você não pode iniciar um chat consigo mesmo.", Toast.LENGTH_SHORT).show()
            return
        }
        progressBar.visibility = View.VISIBLE
        chatViewModel.createDirectChat(otherUserId = user.id)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
