package ipvc.tp.devhive.presentation.viewmodel.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.model.Message
import ipvc.tp.devhive.domain.model.MessageAttachment
import ipvc.tp.devhive.domain.usecase.chat.CreateChatUseCase
import ipvc.tp.devhive.domain.usecase.chat.SendMessageUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch

class ChatViewModel(
    private val createChatUseCase: CreateChatUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _chatEvent = MutableLiveData<Event<ChatEvent>>()
    val chatEvent: LiveData<Event<ChatEvent>> = _chatEvent

    fun createChat(
        name: String,
        creatorUid: String,
        isPrivate: Boolean,
        initialParticipants: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val result = createChatUseCase(
                name = name,
                creatorUid = creatorUid,
                isPrivate = isPrivate,
                initialParticipants = initialParticipants
            )

            result.fold(
                onSuccess = {
                    _chatEvent.value = Event(ChatEvent.CreateSuccess(it))
                },
                onFailure = {
                    _chatEvent.value = Event(ChatEvent.CreateFailure(it.message ?: "Erro ao criar chat"))
                }
            )
        }
    }

    fun sendMessage(
        chatId: String,
        senderUid: String,
        content: String,
        attachments: List<MessageAttachment> = emptyList()
    ) {
        viewModelScope.launch {
            val result = sendMessageUseCase(
                chatId = chatId,
                senderUid = senderUid,
                content = content,
                attachments = attachments
            )

            result.fold(
                onSuccess = {
                    _chatEvent.value = Event(ChatEvent.SendMessageSuccess(it))
                },
                onFailure = {
                    _chatEvent.value = Event(ChatEvent.SendMessageFailure(it.message ?: "Erro ao enviar mensagem"))
                }
            )
        }
    }
}

sealed class ChatEvent {
    data class CreateSuccess(val chat: Chat) : ChatEvent()
    data class CreateFailure(val message: String) : ChatEvent()
    data class SendMessageSuccess(val message: Message) : ChatEvent()
    data class SendMessageFailure(val message: String) : ChatEvent()
}
