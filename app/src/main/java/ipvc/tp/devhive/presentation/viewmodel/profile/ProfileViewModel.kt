package ipvc.tp.devhive.presentation.viewmodel.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import java.util.Date

class ProfileViewModel : ViewModel() {
    // LiveData para o perfil do utilizador
    private val _userProfile = MutableLiveData<User>()
    val userProfile: LiveData<User> = _userProfile

    // LiveData para eventos de perfil
    private val _profileEvent = MutableLiveData<Event<ProfileEvent>>()
    val profileEvent: LiveData<Event<ProfileEvent>> = _profileEvent

    // LiveData para o estado de loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Carrega o perfil do utilizador
    fun loadUserProfile(userId: String) {
        _isLoading.value = true

        // implementação real: buscar dados do repositório
        // Por enquanto, simulamos o carregamento
        viewModelScope.launch {
            try {
                // Simula uma chamada de rede
                kotlinx.coroutines.delay(1000)

                val user = User(
                    id = userId,
                    name = "Diogo Oliveira",
                    username = "diogo",
                    email = "diogo.ol@example.com",
                    profileImageUrl = "",
                    bio = "Estudante de Engenharia Informática no IPVC. Interessado em desenvolvimento mobile e inteligência artificial.",
                    institution = "Instituto Politécnico de Viana do Castelo",
                    course = "Licenciatura em Engenharia Informática",
                    createdAt = Timestamp(Date()),
                    lastLogin = Timestamp(Date()),
                    isOnline = true,
                    contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                        materials = 12,
                        comments = 45,
                        likes = 78,
                        sessions = 5
                    )
                )

                _userProfile.value = user
                _isLoading.value = false
                _profileEvent.value = Event(ProfileEvent.ProfileLoaded)
            } catch (e: Exception) {
                _isLoading.value = false
                _profileEvent.value = Event(ProfileEvent.Error(e.message ?: "Erro ao carregar perfil"))
            }
        }
    }

    // Atualiza o perfil do utilizador
    fun updateProfile(
        name: String,
        bio: String,
        institution: String,
        course: String
    ) {
        _isLoading.value = true

        // implementação real: atualizar dados no repositório
        viewModelScope.launch {
            try {
                // Simula uma chamada de rede
                kotlinx.coroutines.delay(1000)

                // Atualiza o perfil atual
                val currentProfile = _userProfile.value
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(
                        name = name,
                        bio = bio,
                        institution = institution,
                        course = course
                    )
                    _userProfile.value = updatedProfile
                }

                _isLoading.value = false
                _profileEvent.value = Event(ProfileEvent.ProfileUpdated)
            } catch (e: Exception) {
                _isLoading.value = false
                _profileEvent.value = Event(ProfileEvent.Error(e.message ?: "Erro ao atualizar perfil"))
            }
        }
    }
}

sealed class ProfileEvent {
    object ProfileLoaded : ProfileEvent()
    object ProfileUpdated : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}
