package ipvc.tp.devhive.presentation.viewmodel.studygroup

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.usecase.studygroup.CreateStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.DeleteStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.GetStudyGroupByIdUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.GetStudyGroupMessagesUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.GetStudyGroupsByUserUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.LeaveStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.RemoveMemberUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.SendGroupMessageUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.UpdateStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.user.GetCurrentUserUseCase
import ipvc.tp.devhive.domain.usecase.user.GetUsersByIdsUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StudyGroupEvent {
    data class Error(val message: String) : StudyGroupEvent()
}

sealed class StudyGroupGeneralResult {
    data class Success<T>(val data: T? = null) : StudyGroupGeneralResult()
    data class Failure(val message: String) : StudyGroupGeneralResult()
}


@HiltViewModel
class StudyGroupViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getStudyGroupsByUserUseCase: GetStudyGroupsByUserUseCase,
    private val getStudyGroupByIdUseCase: GetStudyGroupByIdUseCase,
    private val getStudyGroupMessagesUseCase: GetStudyGroupMessagesUseCase,
    private val createStudyGroupUseCase: CreateStudyGroupUseCase,
    private val sendGroupMessageUseCase: SendGroupMessageUseCase,
    private val updateStudyGroupUseCase: UpdateStudyGroupUseCase,
    private val deleteStudyGroupUseCase: DeleteStudyGroupUseCase,
    private val removeMemberUseCase: RemoveMemberUseCase,
    private val leaveStudyGroupUseCase: LeaveStudyGroupUseCase,
    private val getUsersByIdsUseCase: GetUsersByIdsUseCase
) : ViewModel() {

    private var hasAttemptedInitialGroupLoadForCurrentUser = false

    private val _userStudyGroups = MediatorLiveData<List<StudyGroup>>()
    val userStudyGroups: LiveData<List<StudyGroup>> = _userStudyGroups
    private var currentUserStudyGroupsSource: LiveData<List<StudyGroup>>? = null

    private val _selectedStudyGroup = MutableLiveData<StudyGroup?>()
    val selectedStudyGroup: LiveData<StudyGroup?> = _selectedStudyGroup

    private val _groupMessages = MediatorLiveData<List<GroupMessage>>()
    val groupMessages: LiveData<List<GroupMessage>> = _groupMessages
    private var currentMessagesSource: LiveData<List<GroupMessage>>? = null

    private val _generalEvent = MutableLiveData<Event<StudyGroupEvent>?>()
    val generalEvent: LiveData<Event<StudyGroupEvent>?> = _generalEvent

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _createGroupResultEvent = MutableLiveData<Event<StudyGroupGeneralResult>>()
    val createGroupResultEvent: LiveData<Event<StudyGroupGeneralResult>> = _createGroupResultEvent

    private val _sendMessageResultEvent = MutableLiveData<Event<StudyGroupGeneralResult>>()
    val sendMessageResultEvent: LiveData<Event<StudyGroupGeneralResult>> = _sendMessageResultEvent

    private val _updateGroupResultEvent = MutableLiveData<Event<StudyGroupGeneralResult>>()
    val updateGroupResultEvent: LiveData<Event<StudyGroupGeneralResult>> = _updateGroupResultEvent

    private val _deleteGroupResultEvent = MutableLiveData<Event<StudyGroupGeneralResult>>()
    val deleteGroupResultEvent: LiveData<Event<StudyGroupGeneralResult>> = _deleteGroupResultEvent

    private val _leaveGroupResultEvent = MutableLiveData<Event<StudyGroupGeneralResult>>()
    val leaveGroupResultEvent: LiveData<Event<StudyGroupGeneralResult>> = _leaveGroupResultEvent

    private val _removeMemberResultEvent = MutableLiveData<Event<StudyGroupGeneralResult>>()
    val removeMemberResultEvent: LiveData<Event<StudyGroupGeneralResult>> = _removeMemberResultEvent

    private val _groupMembersDetails = MutableLiveData<List<User>>()
    val groupMembersDetails: LiveData<List<User>> = _groupMembersDetails

    private val _isCurrentUserAdmin = MutableLiveData(false)
    val isCurrentUserAdmin: LiveData<Boolean> = _isCurrentUserAdmin

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val previousUserId = _currentUser.value?.id
            _currentUser.value = user

            if (user?.id != previousUserId) {
                hasAttemptedInitialGroupLoadForCurrentUser = false
                _userStudyGroups.value = emptyList()
            }
        }
    }

    fun loadUserStudyGroups() {
        val user = _currentUser.value
        if (user == null) {
            _isLoading.value = false
            _generalEvent.value = Event(StudyGroupEvent.Error("Utilizador não autenticado"))
            _userStudyGroups.value = emptyList()
            return
        }

        if (hasAttemptedInitialGroupLoadForCurrentUser && currentUserStudyGroupsSource != null && _isLoading.value == false) {
            Log.d("ViewModel", "loadUserStudyGroups: Already attempted load and have a source. Skipping.")
            return
        }
        if (_isLoading.value == true) {
            Log.d("ViewModel", "loadUserStudyGroups: Already loading. Skipping.")
            return
        }

        Log.d("ViewModel", "loadUserStudyGroups: Proceeding with load for user ${user.id}")
        _isLoading.value = true
        currentUserStudyGroupsSource?.let {
            _userStudyGroups.removeSource(it)
            currentUserStudyGroupsSource = null
        }

        viewModelScope.launch {
            try {
                val newSource = getStudyGroupsByUserUseCase(user.id)
                currentUserStudyGroupsSource = newSource
                hasAttemptedInitialGroupLoadForCurrentUser = true

                _userStudyGroups.addSource(newSource) { updatedGroups ->
                    Log.d("ViewModel", "Groups received from source. Count: ${updatedGroups.size}")
                    if (_userStudyGroups.value != updatedGroups) {
                        _userStudyGroups.value = updatedGroups
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Erro ao carregar grupos do utilizador: ${e.message}", e)
                _isLoading.value = false
                _userStudyGroups.value = emptyList()
                _generalEvent.value = Event(StudyGroupEvent.Error(e.message ?: "Erro ao buscar grupos do utilizador."))
                hasAttemptedInitialGroupLoadForCurrentUser = false
            }
        }
    }

    fun loadGroupMembersDetails(memberIds: List<String>) {
        if (memberIds.isEmpty()) {
            _groupMembersDetails.value = emptyList()
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = getUsersByIdsUseCase(memberIds)
                if (result.isSuccess) {
                    _groupMembersDetails.value = result.getOrNull() ?: emptyList()
                } else {
                    _groupMembersDetails.value = emptyList()
                    _generalEvent.value = Event(StudyGroupEvent.Error(result.exceptionOrNull()?.message ?: "Erro ao buscar detalhes dos membros."))
                }
            } catch (e: Exception) {
                _groupMembersDetails.value = emptyList()
                _generalEvent.value = Event(StudyGroupEvent.Error(e.message ?: "Erro inesperado ao buscar detalhes dos membros."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadStudyGroupDetails(groupId: String) {
        if (groupId.isEmpty()) {
            _generalEvent.value = Event(StudyGroupEvent.Error("ID do grupo inválido."))
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val group = getStudyGroupByIdUseCase(groupId)
                _selectedStudyGroup.value = group
                if (group == null) {
                    _generalEvent.value = Event(StudyGroupEvent.Error("Grupo não encontrado"))
                } else {
                    val adminIds = group.admins
                    _isCurrentUserAdmin.value = _currentUser.value?.id?.let { userId -> adminIds.contains(userId) } ?: false
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Erro ao carregar detalhes do grupo $groupId: ${e.message}", e)
                _selectedStudyGroup.value = null
                _generalEvent.value = Event(StudyGroupEvent.Error(e.message ?: "Erro ao carregar detalhes do grupo."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createStudyGroup(
        name: String,
        description: String,
        isPrivate: Boolean,
        categories: List<String>
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val user = _currentUser.value
            if (user == null) {
                _createGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure("Utilizador não autenticado para criar grupo."))
                _isLoading.value = false
                return@launch
            }
            try {
                val result = createStudyGroupUseCase(
                    name = name,
                    description = description,
                    categories = categories,
                    isPrivate = isPrivate,
                    maxMembers = 50
                )

                if (result.isSuccess) {
                    val newGroup = result.getOrNull()
                    if (newGroup != null) {
                        _createGroupResultEvent.value = Event(StudyGroupGeneralResult.Success(newGroup.id))
                    } else {
                        _createGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure("Grupo criado, mas dados não retornados."))
                    }
                } else {
                    _createGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure(result.exceptionOrNull()?.message ?: "Falha ao criar grupo."))
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Exceção ao criar grupo: ${e.message}", e)
                _createGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure(e.message ?: "Erro inesperado ao criar grupo."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendGroupMessageWithAttachment(
        groupId: String,
        content: String,
        attachmentUri: Uri?,
        originalAttachmentFileName: String?
    ) {
        val user = _currentUser.value
        if (user == null) {
            _sendMessageResultEvent.value = Event(StudyGroupGeneralResult.Failure("Utilizador não autenticado para enviar mensagem."))
            return
        }

        if (content.isBlank() && attachmentUri == null) {
            _sendMessageResultEvent.value = Event(StudyGroupGeneralResult.Failure("A mensagem não pode estar vazia sem um anexo."))
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = sendGroupMessageUseCase(
                    groupId = groupId,
                    content = content,
                    attachmentUri = attachmentUri,
                    originalAttachmentFileName = originalAttachmentFileName
                )

                if (result.isSuccess) {
                    _sendMessageResultEvent.value = Event(StudyGroupGeneralResult.Success("Mensagem enviada"))
                } else {
                    _sendMessageResultEvent.value = Event(
                        StudyGroupGeneralResult.Failure(result.exceptionOrNull()?.message ?: "Falha ao enviar mensagem.")
                    )
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Exceção ao enviar mensagem: ${e.message}", e)
                _sendMessageResultEvent.value = Event(
                    StudyGroupGeneralResult.Failure(e.message ?: "Erro inesperado ao enviar mensagem.")
                )
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadGroupMessages(groupId: String) {
        if (groupId.isEmpty()) return

        currentMessagesSource?.let {
            _groupMessages.removeSource(it)
        }
        viewModelScope.launch {
            try {
                val newSource: LiveData<List<GroupMessage>> = getStudyGroupMessagesUseCase(groupId)
                currentMessagesSource = newSource

                _groupMessages.addSource(newSource) { messageList ->
                    _groupMessages.value = messageList
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Erro ao carregar mensagens do grupo $groupId: ${e.message}", e)
                _groupMessages.value = emptyList()
                _generalEvent.value = Event(StudyGroupEvent.Error(e.message ?: "Erro ao carregar mensagens"))
            }
        }
    }

    fun leaveStudyGroup(groupId: String) {
        val userId = _currentUser.value?.id
        if (userId == null) {
            _leaveGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure("Utilizador não autenticado."))
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = leaveStudyGroupUseCase(groupId, userId)
                if (result.isSuccess) {
                    _leaveGroupResultEvent.value = Event(StudyGroupGeneralResult.Success("Você saiu do grupo."))
                } else {
                    _leaveGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure(result.exceptionOrNull()?.message ?: "Falha ao sair do grupo."))
                }
            } catch (e: Exception) {
                _leaveGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure(e.message ?: "Erro inesperado ao sair do grupo."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStudyGroupDetails(
        groupId: String,
        name: String,
        description: String,
        categories: List<String>,
        newImageUri: Uri?
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = updateStudyGroupUseCase(
                    groupId = groupId,
                    name = name,
                    description = description,
                    categories = categories,
                    imageUri = newImageUri
                )
                if (result.isSuccess) {
                    _updateGroupResultEvent.value = Event(StudyGroupGeneralResult.Success("Grupo atualizado com sucesso"))
                    loadStudyGroupDetails(groupId)
                } else {
                    _updateGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure(result.exceptionOrNull()?.message ?: "Falha ao atualizar grupo."))
                }
            } catch (e: Exception) {
                _updateGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure(e.message ?: "Erro inesperado ao atualizar grupo."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteStudyGroup(groupId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = deleteStudyGroupUseCase(groupId)
                if (result.isSuccess) {
                    _deleteGroupResultEvent.value = Event(StudyGroupGeneralResult.Success("Grupo excluído com sucesso"))
                } else {
                    _deleteGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure(result.exceptionOrNull()?.message ?: "Falha ao excluir grupo."))
                }
            } catch (e: Exception) {
                _deleteGroupResultEvent.value = Event(StudyGroupGeneralResult.Failure(e.message ?: "Erro inesperado ao excluir grupo."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeMemberFromGroup(groupId: String, memberIdToRemove: String) {
        val currentUserId = _currentUser.value?.id
        if (currentUserId == null) {
            _removeMemberResultEvent.value = Event(StudyGroupGeneralResult.Failure("Utilizador não autenticado."))
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = removeMemberUseCase(groupId, memberIdToRemove)
                if (result.isSuccess) {
                    _removeMemberResultEvent.value = Event(StudyGroupGeneralResult.Success("Membro removido com sucesso."))
                    loadStudyGroupDetails(groupId)
                } else {
                    _removeMemberResultEvent.value = Event(StudyGroupGeneralResult.Failure(result.exceptionOrNull()?.message ?: "Falha ao remover membro."))
                }
            } catch (e: Exception) {
                _removeMemberResultEvent.value = Event(StudyGroupGeneralResult.Failure(e.message ?: "Erro inesperado ao remover membro."))
            } finally {
                _isLoading.value = false
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        currentUserStudyGroupsSource?.let { _userStudyGroups.removeSource(it) }
        currentMessagesSource?.let { _groupMessages.removeSource(it) }
    }
}