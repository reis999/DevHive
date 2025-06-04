package ipvc.tp.devhive.presentation.viewmodel.studygroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.domain.model.MessageAttachment
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.usecase.studygroup.CreateStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.JoinStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.SendGroupMessageUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StudyGroupViewModel @Inject constructor(
    private val createStudyGroupUseCase: CreateStudyGroupUseCase,
    private val joinStudyGroupUseCase: JoinStudyGroupUseCase,
    private val sendGroupMessageUseCase: SendGroupMessageUseCase
) : ViewModel() {
    // LiveData para a lista de grupos de estudo
    private val _studyGroups = MutableLiveData<List<StudyGroup>>()
    val studyGroups: LiveData<List<StudyGroup>> = _studyGroups

    // LiveData para um grupo de estudo específico
    private val _studyGroup = MutableLiveData<StudyGroup>()
    val studyGroup: LiveData<StudyGroup> = _studyGroup

    // LiveData para eventos de grupo de estudo
    private val _studyGroupEvent = MutableLiveData<Event<StudyGroupEvent>>()
    val studyGroupEvent: LiveData<Event<StudyGroupEvent>> = _studyGroupEvent

    // LiveData para o estado de loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _studyGroupCreationResult = MutableLiveData<Event<Result<StudyGroup>>>()
    val studyGroupCreationResult: LiveData<Event<Result<StudyGroup>>> = _studyGroupCreationResult

    private val _joinStudyGroupResult = MutableLiveData<Event<Result<Boolean>>>()
    val joinStudyGroupResult: LiveData<Event<Result<Boolean>>> = _joinStudyGroupResult

    private val _sendMessageResult = MutableLiveData<Event<Result<GroupMessage>>>()
    val sendMessageResult: LiveData<Event<Result<GroupMessage>>> = _sendMessageResult

    // Carrega a lista de grupos de estudo
    fun loadStudyGroups() {
        _isLoading.value = true

        // implementação real: buscar dados do repositório
        viewModelScope.launch {
            try {
                // Simula uma chamada de rede
                kotlinx.coroutines.delay(1000)

                _studyGroups.value = emptyList()
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _studyGroupEvent.value = Event(StudyGroupEvent.Error(e.message ?: "Erro ao carregar grupos"))
            }
        }
    }

    // Carrega um grupo de estudo específico
    fun loadStudyGroup(groupId: String) {
        _isLoading.value = true

        // implementação real: buscar dados do repositório
        viewModelScope.launch {
            try {
                // Simula uma chamada de rede
                kotlinx.coroutines.delay(1000)

                val group = StudyGroup(
                    id = groupId,
                    name = "Grupo de Programação Java",
                    description = "Grupo dedicado ao estudo e prática de programação em Java.",
                    createdBy = "user123",
                    createdAt = Timestamp(Date()),
                    updatedAt = Timestamp(Date()),
                    imageUrl = "",
                    members = listOf("user123", "user456", "user789"),
                    admins = listOf("user123"),
                    categories = listOf("Programação", "Java", "OOP"),
                    isPrivate = false,
                    joinCode = ""
                )

                _studyGroup.value = group
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _studyGroupEvent.value = Event(StudyGroupEvent.Error(e.message ?: "Erro ao carregar grupo"))
            }
        }
    }

    // Cria um novo grupo de estudo
    fun createStudyGroup(
        name: String,
        description: String,
        creatorUid: String,
        isPrivate: Boolean,
        categories: List<String>
    ) {
        _isLoading.value = true

        // implementação real: salvar dados no repositório
        viewModelScope.launch {
            try {
                // Simula uma chamada de rede
                kotlinx.coroutines.delay(1000)

                val groupId = UUID.randomUUID().toString()
                val now = Date()
                val joinCode = if (isPrivate) {
                    (1..6).map { ('0'..'9').random() }.joinToString("")
                } else {
                    ""
                }

                val newGroup = StudyGroup(
                    id = groupId,
                    name = name,
                    description = description,
                    createdBy = creatorUid,
                    createdAt = Timestamp(now),
                    updatedAt = Timestamp(now),
                    imageUrl = "",
                    members = listOf(creatorUid),
                    admins = listOf(creatorUid),
                    categories = categories,
                    isPrivate = isPrivate,
                    joinCode = joinCode
                )

                val currentGroups = _studyGroups.value ?: emptyList()
                _studyGroups.value = currentGroups + newGroup

                _isLoading.value = false
                _studyGroupEvent.value = Event(StudyGroupEvent.CreateSuccess(newGroup))
            } catch (e: Exception) {
                _isLoading.value = false
                _studyGroupEvent.value = Event(StudyGroupEvent.CreateFailure(e.message ?: "Erro ao criar grupo"))
            }
        }
    }

    // Entra em um grupo de estudo
    fun joinStudyGroup(groupId: String, userId: String, joinCode: String = "") {
        _isLoading.value = true

        // implementação real: atualizar dados no repositório
        viewModelScope.launch {
            try {
                // Simula uma chamada de rede
                kotlinx.coroutines.delay(1000)

                // Verifica se o grupo existe
                val currentGroup = _studyGroup.value
                if (currentGroup != null && currentGroup.id == groupId) {
                    // Verifica se é um grupo privado e se o código de acesso está correto
                    if (currentGroup.isPrivate && currentGroup.joinCode != joinCode) {
                        throw Exception("Código de acesso inválido")
                    }

                    // Adiciona o utilizador à lista de membros
                    val updatedMembers = currentGroup.members.toMutableList()
                    if (!updatedMembers.contains(userId)) {
                        updatedMembers.add(userId)
                    }

                    val updatedGroup = currentGroup.copy(
                        members = updatedMembers,
                        updatedAt = Timestamp(Date())
                    )

                    _studyGroup.value = updatedGroup

                    // Atualiza o grupo na lista geral
                    val currentGroups = _studyGroups.value ?: emptyList()
                    val updatedGroups = currentGroups.map {
                        if (it.id == groupId) updatedGroup else it
                    }
                    _studyGroups.value = updatedGroups

                    _isLoading.value = false
                    _studyGroupEvent.value = Event(StudyGroupEvent.JoinSuccess)
                } else {
                    throw Exception("Grupo não encontrado")
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _studyGroupEvent.value = Event(StudyGroupEvent.JoinFailure(e.message ?: "Erro ao entrar no grupo"))
            }
        }
    }

    fun createStudyGroup(
        name: String,
        description: String,
        categories: List<String>,
        isPrivate: Boolean,
        maxMembers: Int
    ) {
        viewModelScope.launch {
            val result = createStudyGroupUseCase(
                name = name,
                description = description,
                categories = categories,
                isPrivate = isPrivate,
                maxMembers = maxMembers
            )
            _studyGroupCreationResult.value = Event(result)
        }
    }

    fun joinStudyGroup(groupId: String) {
        viewModelScope.launch {
            val result = joinStudyGroupUseCase.joinByGroupId(groupId)
            _joinStudyGroupResult.value = Event(result)
        }
    }

    fun joinStudyGroupByCode(joinCode: String) {
        viewModelScope.launch {
            val result = joinStudyGroupUseCase.joinByJoinCode(joinCode)
            _joinStudyGroupResult.value = Event(result)
        }
    }

    fun sendGroupMessage(groupId: String, content: String, attachments: List<MessageAttachment> = emptyList()) {
        viewModelScope.launch {
            val result = sendGroupMessageUseCase(
                groupId = groupId,
                content = content,
                attachments = attachments
            )
            _sendMessageResult.value = Event(result)
        }
    }
}

sealed class StudyGroupEvent {
    data class CreateSuccess(val studyGroup: StudyGroup) : StudyGroupEvent()
    data class CreateFailure(val message: String) : StudyGroupEvent()
    object JoinSuccess : StudyGroupEvent()
    data class JoinFailure(val message: String) : StudyGroupEvent()
    data class Error(val message: String) : StudyGroupEvent()
}
