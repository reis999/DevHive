package ipvc.tp.devhive.presentation.ui.main.chat

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.DevHiveApp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.viewmodel.chat.ChatViewModel

class CreateChatActivity : AppCompatActivity() {

    private lateinit var chatViewModel: ChatViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var btnCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_chat)

        // Inicializa as views
        toolbar = findViewById(R.id.toolbar)
        btnCreate = findViewById(R.id.btn_create)

        // Configura a toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.create_chat)

        // Inicializa o ViewModel
        val factory = DevHiveApp.getViewModelFactories().chatViewModelFactory
        chatViewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]


        // Configura o botão de criar
        btnCreate.setOnClickListener {
            createChat()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createChat() {
        // implementação real: usar chatViewModel.createChat()
        Toast.makeText(this, R.string.chat_created_success, Toast.LENGTH_SHORT).show()
        finish()
    }
}
