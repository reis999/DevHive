package ipvc.tp.devhive.domain.usecase.auth

import ipvc.tp.devhive.domain.repository.AuthRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para autenticar um usuário
 */
class LoginUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email e senha são obrigatórios"))
        }

        val result = authRepository.login(email, password)
        if (result.isSuccess) {
            val userId = result.getOrThrow()
            userRepository.updateUserOnlineStatus(userId, true)
        }

        return result
    }
}


