package ipvc.tp.devhive.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.domain.usecase.auth.LoginUserUseCase
import ipvc.tp.devhive.domain.usecase.auth.LogoutUserUseCase
import ipvc.tp.devhive.domain.usecase.auth.RegisterUserUseCase

class AuthViewModelFactory(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                registerUserUseCase,
                loginUserUseCase,
                logoutUserUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
