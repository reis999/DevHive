package ipvc.tp.devhive.presentation.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.domain.usecase.chat.CreateChatUseCase
import ipvc.tp.devhive.domain.usecase.chat.SendMessageUseCase

class ChatViewModelFactory(
    private val createChatUseCase: CreateChatUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                createChatUseCase,
                sendMessageUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
