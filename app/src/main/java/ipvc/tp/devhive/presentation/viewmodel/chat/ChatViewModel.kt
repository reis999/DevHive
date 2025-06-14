package ipvc.tp.devhive.presentation.viewmodel.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.model.Message
import ipvc.tp.devhive.domain.model.MessageAttachment
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.usecase.chat.CreateChatUseCase
import ipvc.tp.devhive.domain.usecase.chat.DeleteChatUseCase
import ipvc.tp.devhive.domain.usecase.chat.GetChatByIdUseCase
import ipvc.tp.devhive.domain.usecase.chat.GetChatsByUserUseCase
import ipvc.tp.devhive.domain.usecase.chat.GetMessagesByChatIdUseCase
import ipvc.tp.devhive.domain.usecase.chat.SendMessageUseCase
import ipvc.tp.devhive.domain.usecase.user.GetCurrentUserUseCase
import ipvc.tp.devhive.domain.usecase.user.GetUserByIdUseCase
import ipvc.tp.devhive.domain.usecase.user.SearchUsersUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val createChatUseCase: CreateChatUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val deleteChatUseCase: DeleteChatUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val getChatsByUserUseCase: GetChatsByUserUseCase,
    private val getMessagesByChatIdUseCase: GetMessagesByChatIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val searchUsersUseCase: SearchUsersUseCase
    ) : ViewModel() {

    private val _chatEvent = MutableLiveData<Event<ChatEvent>>()
    val chatEvent: LiveData<Event<ChatEvent>> = _chatEvent

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _userChats = MutableLiveData<List<Chat>>()
    val userChats: LiveData<List<Chat>> = _userChats

    private val _selectedChat = MutableLiveData<Chat?>()
    val selectedChat: LiveData<Chat?> = _selectedChat

    private val _chatMessages = MutableLiveData<List<Message>>()
    val chatMessages: LiveData<List<Message>> = _chatMessages

    private val _otherUserDetails = MutableLiveData<User?>()
    val otherUserDetails: LiveData<User?> = _otherUserDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchedUsers = MutableLiveData<List<User>>()
    val searchedUsers: LiveData<List<User>> = _searchedUsers

    private val _isUserSearchLoading = MutableLiveData<Boolean>()
    val isUserSearchLoading: LiveData<Boolean> = _isUserSearchLoading

    private var currentUserId: String? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            _currentUser.value = user
            currentUserId = user?.id
            if (user == null) {
                _chatEvent.value = Event(ChatEvent.Error("Usuário não autenticado."))
            }
        }
    }

    fun createDirectChat(otherUserId: String) {
        val currentUId = currentUserId
        if (currentUId == otherUserId) {
            _chatEvent.value =
                Event(ChatEvent.CreateFailure("Não é possível criar um chat consigo mesmo."))
            return
        }

        _isLoading.value = true
        if (currentUId != null) {
            viewModelScope.launch {
                val result = createChatUseCase(
                    currentUserId = currentUId,
                    otherUserId = otherUserId
                )
                result.fold(
                    onSuccess = { chat ->
                        _chatEvent.value = Event(ChatEvent.CreateSuccess(chat))
                    },
                    onFailure = {
                        _chatEvent.value =
                            Event(ChatEvent.CreateFailure(it.message ?: "Erro ao criar chat"))
                    }
                )
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(
        chatId: String,
        content: String,
        attachments: List<MessageAttachment> = emptyList()
    ) {
        val senderUId = currentUserId
        if (content.isBlank() && attachments.isEmpty()) {
            _chatEvent.value = Event(ChatEvent.SendMessageFailure("A mensagem não pode estar vazia."))
            return
        }

        if (senderUId == null) {
            _chatEvent.value = Event(ChatEvent.SendMessageFailure("Utilizador não autenticado."))
            return
        }

        viewModelScope.launch {
            val result = sendMessageUseCase(
                chatId = chatId,
                senderUid = senderUId,
                content = content,
                attachments = attachments
            )
            result.fold(
                onSuccess = { message ->
                    _chatEvent.value = Event(ChatEvent.SendMessageSuccess(message))
                },
                onFailure = {
                    _chatEvent.value = Event(ChatEvent.SendMessageFailure(it.message ?: "Erro ao enviar mensagem"))
                }
            )
        }
    }

    fun loadUserChats(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                getChatsByUserUseCase(userId).observeForever { chats ->
                    _userChats.value = chats
                }
            } catch (e: Exception) {
                _userChats.value = emptyList() // Limpa em caso de erro
                _chatEvent.value = Event(ChatEvent.Error(e.message ?: "Erro ao carregar chats"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadChatDetailsAndMessages(chatId: String) {
        if (chatId.isEmpty()) {
            _chatEvent.value = Event(ChatEvent.Error("ID do chat inválido."))
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val chat = getChatByIdUseCase(chatId)
                _selectedChat.value = chat
                if (chat != null) {
                    val otherParticipantId = if (chat.participant1Id == currentUserId) {
                        chat.participant2Id
                    } else {
                        chat.participant1Id
                    }
                    val otherUser = getUserByIdUseCase(otherParticipantId)
                    _otherUserDetails.value = otherUser

                    getMessagesByChatIdUseCase(chatId).observeForever { messages ->
                        _chatMessages.value = messages
                    }
                } else {
                    _chatEvent.value = Event(ChatEvent.Error("Chat não encontrado."))
                }
            } catch (e: Exception) {
                _selectedChat.value = null
                _chatMessages.value = emptyList()
                _otherUserDetails.value = null
                _chatEvent.value = Event(ChatEvent.Error(e.message ?: "Erro ao carregar detalhes do chat ou mensagens"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun listenForMessages(chatId: String) {
        if (chatId.isEmpty()) return
        viewModelScope.launch {
            try {
                getMessagesByChatIdUseCase(chatId).observeForever { messages ->
                    _chatMessages.value = messages
                }
            } catch (e: Exception) {
                _chatMessages.value = emptyList() // Limpa em caso de erro
                _chatEvent.value = Event(ChatEvent.Error(e.message ?: "Erro ao carregar mensagens"))
            }
        }
    }

    fun loadAllUsersForSelection(excludeUserId: String?) {
        _isUserSearchLoading.value = true
        viewModelScope.launch {
            val result = searchUsersUseCase("", excludeUserId)
            result.fold(
                onSuccess = { users -> _searchedUsers.value = users },
                onFailure = {
                    _searchedUsers.value = emptyList()
                    _chatEvent.value = Event(ChatEvent.Error(it.message ?: "Erro ao carregar usuários"))
                }
            )
            _isUserSearchLoading.value = false
        }
    }

    fun searchUsers(query: String) {
        val currentUId = currentUserId
        if (query.length < 2 && query.isNotEmpty()) {
            _searchedUsers.value = emptyList()
            return
        }
        if (query.isEmpty()) {
            loadAllUsersForSelection(currentUId)
            return
        }

        _isUserSearchLoading.value = true
        viewModelScope.launch {
            val result = searchUsersUseCase(query, currentUId)
            result.fold(
                onSuccess = { users -> _searchedUsers.value = users },
                onFailure = {
                    _searchedUsers.value = emptyList()
                    _chatEvent.value = Event(ChatEvent.Error(it.message ?: "Erro ao buscar usuários"))
                }
            )
            _isUserSearchLoading.value = false
        }
    }

}

sealed class ChatEvent {
    data class CreateSuccess(val chat: Chat) : ChatEvent()
    data class CreateFailure(val message: String) : ChatEvent()
    data class SendMessageSuccess(val message: Message) : ChatEvent()
    data class SendMessageFailure(val message: String) : ChatEvent()
    data class Error(val message: String) : ChatEvent()
}
