package ipvc.tp.devhive.presentation.viewmodel.studygroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.usecase.studygroup.GetPublicStudyGroupsUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.JoinStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.user.GetCurrentUserUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreGroupsViewModel @Inject constructor(
    private val getPublicStudyGroupsUseCase: GetPublicStudyGroupsUseCase,
    private val joinStudyGroupUseCase: JoinStudyGroupUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _publicGroups = MediatorLiveData<List<StudyGroup>>()
    val publicGroups: LiveData<List<StudyGroup>> get() = _publicGroups

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _event = MutableLiveData<Event<ExploreEvent>>()
    val event: LiveData<Event<ExploreEvent>> get() = _event

    private var publicGroupsSource: LiveData<List<StudyGroup>>? = null

    init {
        loadPublicGroups()
    }

    private fun loadPublicGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUser = getCurrentUserUseCase()
            val currentUserId = currentUser?.id

            publicGroupsSource?.let { _publicGroups.removeSource(it) }

            publicGroupsSource = getPublicStudyGroupsUseCase()

            _publicGroups.addSource(publicGroupsSource!!) { groups ->
                val filteredGroups = groups.filterNot { group ->
                    currentUserId != null && group.members.any { member -> member == currentUserId }
                }
                _publicGroups.value = filteredGroups
                _isLoading.value = false
            }
        }
    }

    fun joinPublicGroup(groupId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                _event.value = Event(ExploreEvent.JoinFailure("Utilizador não autenticado."))
                _isLoading.value = false
                return@launch
            }

            val result = joinStudyGroupUseCase.joinByGroupId(groupId)
            if (result.isSuccess) {
                _event.value = Event(ExploreEvent.JoinSuccess("Entrou no grupo com sucesso!"))
            } else {
                _event.value = Event(ExploreEvent.JoinFailure(result.exceptionOrNull()?.message ?: "Erro ao entrar no grupo."))
            }
            _isLoading.value = false
        }
    }

    fun joinPrivateGroup(joinCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                _event.value = Event(ExploreEvent.JoinFailure("Utilizador não autenticado."))
                _isLoading.value = false
                return@launch
            }

            if (joinCode.isBlank()) {
                _event.value = Event(ExploreEvent.JoinFailure("Código de acesso não pode estar vazio."))
                _isLoading.value = false
                return@launch
            }

            val result = joinStudyGroupUseCase.joinByJoinCode(joinCode)
            if (result.isSuccess) {
                _event.value = Event(ExploreEvent.JoinSuccess("Entrou no grupo com sucesso!"))
            } else {
                _event.value = Event(ExploreEvent.JoinFailure(result.exceptionOrNull()?.message ?: "Código de acesso inválido ou erro."))
            }
            _isLoading.value = false
        }
    }
}

sealed class ExploreEvent {
    data class JoinSuccess(val message: String) : ExploreEvent()
    data class JoinFailure(val message: String) : ExploreEvent()
}