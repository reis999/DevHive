package ipvc.tp.devhive.presentation.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipvc.tp.devhive.domain.usecase.auth.LoginUserUseCase
import ipvc.tp.devhive.domain.usecase.auth.LogoutUserUseCase
import ipvc.tp.devhive.domain.usecase.auth.RegisterUserUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch

class AuthViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _authEvent = MutableLiveData<Event<AuthEvent>>()
    val authEvent: LiveData<Event<AuthEvent>> = _authEvent

    init {
        // Verifica se o usuário já está autenticado
        checkAuthState()
    }

    private fun checkAuthState() {
        // Aqui seria implementada a verificação de autenticação com Firebase Auth
        // Por enquanto, definimos como não autenticado
        _authState.value = AuthState.Unauthenticated
    }

    fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        institution: String,
        course: String
    ) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = registerUserUseCase(
                name = name,
                username = username,
                email = email,
                password = password,
                institution = institution,
                course = course
            )

            result.fold(
                onSuccess = {
                    _authState.value = AuthState.Authenticated(it.id)
                    _authEvent.value = Event(AuthEvent.RegisterSuccess)
                },
                onFailure = {
                    _authState.value = AuthState.Error(it.message ?: "Erro ao registrar")
                    _authEvent.value = Event(AuthEvent.RegisterFailure(it.message ?: "Erro ao registrar"))
                }
            )
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = loginUserUseCase(email, password)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState.Authenticated(it)
                    _authEvent.value = Event(AuthEvent.LoginSuccess)
                },
                onFailure = {
                    _authState.value = AuthState.Error(it.message ?: "Erro ao fazer login")
                    _authEvent.value = Event(AuthEvent.LoginFailure(it.message ?: "Erro ao fazer login"))
                }
            )
        }
    }

    fun logout() {
        val currentState = _authState.value
        if (currentState is AuthState.Authenticated) {
            _authState.value = AuthState.Loading

            viewModelScope.launch {
                val result = logoutUserUseCase(currentState.userId)

                result.fold(
                    onSuccess = {
                        _authState.value = AuthState.Unauthenticated
                        _authEvent.value = Event(AuthEvent.LogoutSuccess)
                    },
                    onFailure = {
                        _authState.value = currentState
                        _authEvent.value = Event(AuthEvent.LogoutFailure(it.message ?: "Erro ao fazer logout"))
                    }
                )
            }
        }
    }

    fun resetPassword(email: String) {
        // Implementação da recuperação de senha
        _authEvent.value = Event(AuthEvent.PasswordResetSent)
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class AuthEvent {
    object RegisterSuccess : AuthEvent()
    data class RegisterFailure(val message: String) : AuthEvent()
    object LoginSuccess : AuthEvent()
    data class LoginFailure(val message: String) : AuthEvent()
    object LogoutSuccess : AuthEvent()
    data class LogoutFailure(val message: String) : AuthEvent()
    object PasswordResetSent : AuthEvent()
}
