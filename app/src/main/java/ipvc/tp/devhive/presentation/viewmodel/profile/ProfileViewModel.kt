package ipvc.tp.devhive.presentation.viewmodel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.usecase.auth.LogoutUserUseCase
import ipvc.tp.devhive.domain.usecase.user.GetCurrentUserUseCase
import ipvc.tp.devhive.domain.usecase.user.UpdateUserUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
) : ViewModel() {

    // LiveData para o perfil do usuário
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    // LiveData para eventos de perfil
    private val _profileEvent = MutableLiveData<Event<ProfileEvent>>()
    val profileEvent: LiveData<Event<ProfileEvent>> = _profileEvent

    // LiveData para o estado de loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Carrega o perfil do usuário
    fun loadUserProfile() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase()

                if (user != null) {
                    _userProfile.value = user
                    _profileEvent.value = Event(ProfileEvent.ProfileLoaded)
                } else {
                    _profileEvent.value = Event(ProfileEvent.Error("Utilizador não encontrado"))
                }
            } catch (e: Exception) {
                _profileEvent.value = Event(ProfileEvent.Error("Erro ao carregar perfil: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Atualiza o perfil do usuário
    fun updateProfile(name: String, bio: String, institution: String, course: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val currentUser = _userProfile.value
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        name = name,
                        bio = bio,
                        institution = institution,
                        course = course
                    )

                    val result = updateUserUseCase(updatedUser)
                    if (result.isSuccess) {
                        _userProfile.value = updatedUser
                        _profileEvent.value = Event(ProfileEvent.ProfileUpdated)
                    } else {
                        _profileEvent.value = Event(ProfileEvent.Error("Erro ao atualizar perfil"))
                    }
                }
            } catch (e: Exception) {
                _profileEvent.value = Event(ProfileEvent.Error("Erro ao atualizar perfil: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(){
        viewModelScope.launch {

            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                val logoutResult = logoutUserUseCase(currentUser.id)
                if (logoutResult.isSuccess) {
                    _profileEvent.value = Event(ProfileEvent.LogoutSuccess) // Emitir sucesso
                    Log.d("ViewModel", "Logout successful in use case.")
                } else {
                    _profileEvent.value = Event(ProfileEvent.Error("Falha ao fazer logout: ${logoutResult.exceptionOrNull()?.message}"))
                }
            } else {
                Log.d("ViewModel", "No current user found, attempting Firebase signOut directly.")
                _profileEvent.value = Event(ProfileEvent.LogoutSuccess) // Emitir sucesso
            }
        }
    }
}

sealed class ProfileEvent {
    object ProfileLoaded : ProfileEvent()
    object ProfileUpdated : ProfileEvent()
    object LogoutSuccess : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}

