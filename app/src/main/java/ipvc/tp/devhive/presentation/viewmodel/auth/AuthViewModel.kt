package ipvc.tp.devhive.presentation.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.usecase.auth.LoginUserUseCase
import ipvc.tp.devhive.domain.usecase.auth.LogoutUserUseCase
import ipvc.tp.devhive.domain.usecase.auth.RegisterUserUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _authEvent = MutableLiveData<Event<AuthEvent>>()
    val authEvent: LiveData<Event<AuthEvent>> = _authEvent

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            if (_authState.value !is AuthState.Authenticated || (_authState.value as? AuthState.Authenticated)?.userId != firebaseUser.uid) {
                if (_authState.value is AuthState.Loading && _authEvent.value?.peekContent() is AuthEvent.LoginSuccess || _authEvent.value?.peekContent() is AuthEvent.RegisterSuccess) {
                } else {
                    _authState.value = AuthState.Authenticated(firebaseUser.uid)
                }
            }
        } else {
            if (_authState.value !is AuthState.Unauthenticated && _authState.value !is AuthState.Loading) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    private fun checkAuthState() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            _authState.value = AuthState.Authenticated(currentUserId)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null && _authState.value is AuthState.Authenticated
    }

    fun getCurrentUserId(): String? {
        // Idem
        return if (_authState.value is AuthState.Authenticated) {
            (authState.value as AuthState.Authenticated).userId
        } else {
            firebaseAuth.currentUser?.uid
        }
    }

    fun refreshAuthState() {
        if (firebaseAuth.currentUser != null) {
            _authState.value = AuthState.Authenticated(firebaseAuth.currentUser!!.uid)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
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
        val currentAuthValue = _authState.value
        if (currentAuthValue is AuthState.Authenticated) {
            _authState.value = AuthState.Loading

            viewModelScope.launch {
                val result = logoutUserUseCase(currentAuthValue.userId)

                result.fold(
                    onSuccess = {
                        _authState.value = AuthState.Unauthenticated
                        _authEvent.value = Event(AuthEvent.LogoutSuccess)
                    },
                    onFailure = {
                        _authState.value = currentAuthValue // Reverte para o estado autenticado anterior
                        _authEvent.value = Event(AuthEvent.LogoutFailure(it.message ?: "Erro ao fazer logout"))
                    }
                )
            }
        } else {
            refreshAuthState()
        }
    }

    fun resetPassword(email: String) {
        // TODO: Implementar a lógica para enviar um email de recuperação de senha
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