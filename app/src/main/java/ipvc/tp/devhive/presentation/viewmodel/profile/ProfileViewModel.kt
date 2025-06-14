package ipvc.tp.devhive.presentation.viewmodel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.usecase.auth.LogoutUserUseCase
import ipvc.tp.devhive.domain.usecase.material.GetUserFavoriteMaterialsUseCase
import ipvc.tp.devhive.domain.usecase.user.GetCurrentUserUseCase
import ipvc.tp.devhive.domain.usecase.user.GetUserByIdUseCase
import ipvc.tp.devhive.domain.usecase.user.UpdateUserUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getMaterialsByOwnerUseCase: GetMaterialsByOwnerUseCase,
    private val getUserFavoriteMaterialsUseCase: GetUserFavoriteMaterialsUseCase
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

    // --- LiveData para o USUÁRIO VISUALIZADO (usado pela UserProfileActivity) ---
    private val _viewedUserProfile = MutableLiveData<User?>()
    val viewedUserProfile: LiveData<User?> = _viewedUserProfile

    private val _viewedProfileMaterials = MutableLiveData<List<Material>>()
    val viewedProfileMaterials: LiveData<List<Material>> = _viewedProfileMaterials

    private val _viewedProfileFavorites = MutableLiveData<List<Material>>()
    val viewedProfileFavorites: LiveData<List<Material>> = _viewedProfileFavorites

    private val _viewedProfileEvent = MutableLiveData<Event<ViewedProfileEvent>>()
    val viewedProfileEvent: LiveData<Event<ViewedProfileEvent>> = _viewedProfileEvent

    private val _isLoadingViewedProfile = MutableLiveData<Boolean>()
    val isLoadingViewedProfile: LiveData<Boolean> = _isLoadingViewedProfile

    private val _isLoadingViewedMaterials = MutableLiveData<Boolean>()
    val isLoadingViewedMaterials: LiveData<Boolean> = _isLoadingViewedMaterials


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
                    _profileEvent.value = Event(ProfileEvent.LogoutSuccess)
                    Log.d("ViewModel", "Logout successful in use case.")
                } else {
                    _profileEvent.value = Event(ProfileEvent.Error("Falha ao fazer logout: ${logoutResult.exceptionOrNull()?.message}"))
                }
            } else {
                Log.d("ViewModel", "No current user found, attempting Firebase signOut directly.")
                _profileEvent.value = Event(ProfileEvent.LogoutSuccess)
            }
        }
    }


    // --- Funções para o USUÁRIO VISUALIZADO (UserProfileActivity) ---

    /**
     * Carrega o perfil de um usuário específico pelo seu ID.
     * Usado por UserProfileActivity.
     */
    fun loadUserProfileById(userId: String) {
        _isLoadingViewedProfile.value = true
        _viewedUserProfile.value = null
        viewModelScope.launch {
            try {
                val result = getUserByIdUseCase(userId)
                result.fold(
                    onSuccess = { user ->
                        _viewedUserProfile.value = user
                        loadMaterialsForViewedUser(userId)
                    },
                    onFailure = {
                        _viewedProfileEvent.value = Event(ViewedProfileEvent.Error("Utilizador não encontrado: ${it.message}"))
                    }
                )
            } catch (e: Exception) {
                _viewedProfileEvent.value = Event(ViewedProfileEvent.Error("Erro ao carregar perfil: ${e.message}"))
            } finally {
                _isLoadingViewedProfile.value = false
            }
        }
    }

    /**
     * Carrega os materiais postados pelo usuário visualizado.
     * Usado por UserProfileActivity.
     */
    fun loadMaterialsForViewedUser(userId: String) {
        _isLoadingViewedMaterials.value = true
        _viewedProfileMaterials.value = emptyList()
        viewModelScope.launch {
            try {
                val result = getMaterialsByOwnerUseCase(userId)
                result.fold(
                    onSuccess = { materials ->
                        _viewedProfileMaterials.value = materials
                    },
                    onFailure = {
                        _viewedProfileEvent.value = Event(ViewedProfileEvent.Error("Erro ao carregar materiais: ${it.message}"))
                    }
                )
            } catch (e: Exception) {
                _viewedProfileEvent.value = Event(ViewedProfileEvent.Error("Erro ao carregar materiais: ${e.message}"))
            } finally {
                _isLoadingViewedMaterials.value = false
            }
        }
    }

    /**
     * Carrega os materiais favoritados pelo usuário visualizado.
     * Usado por UserProfileActivity.
     */
    fun loadFavoritesForViewedUser(userId: String) {
        _isLoadingViewedMaterials.value = true
        _viewedProfileFavorites.value = emptyList()
        viewModelScope.launch {
            try {
                val result = getUserFavoriteMaterialsUseCase(userId)
                result.fold(
                    onSuccess = { materials ->
                        val loggedInUserId = _userProfile.value?.id
                        val processedMaterials = materials.map { material ->
                            material.copy(bookmarked = loggedInUserId?.let { material.bookmarkedBy.contains(it) } ?: false)
                        }
                        _viewedProfileFavorites.value = processedMaterials
                    },
                    onFailure = {
                        _viewedProfileEvent.value = Event(ViewedProfileEvent.Error("Erro ao carregar favoritos: ${it.message}"))
                    }
                )
            } catch (e: Exception) {
                _viewedProfileEvent.value = Event(ViewedProfileEvent.Error("Erro ao carregar favoritos: ${e.message}"))
            } finally {
                _isLoadingViewedMaterials.value = false
            }
        }
    }

    /**
     * Chamado quando o ViewModel está prestes a ser destruído.
     * Útil se houver observadores que precisam ser removidos ou recursos liberados.
     */
    override fun onCleared() {
        super.onCleared()
    }
}

sealed class ProfileEvent {
    object ProfileLoaded : ProfileEvent()
    object ProfileUpdated : ProfileEvent()
    object LogoutSuccess : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}

sealed class ViewedProfileEvent {
    data class BookmarkToggled(val material: Material) : ViewedProfileEvent()
    data class Error(val message: String) : ViewedProfileEvent()
}
