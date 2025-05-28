package ipvc.tp.devhive.domain.usecase.auth

import ipvc.tp.devhive.domain.repository.UserRepository

/**
 * Caso de uso para autenticar um usuário
 */
class LoginUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(email: String, password: String): Result<String> {
        // Validação de dados
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email e senha são obrigatórios"))
        }

        // Falta implementada a lógica de autenticação com Firebase Auth
        val userId = "user123"

        // Atualiza o status online e o último login
        userRepository.updateUserOnlineStatus(userId, true)

        return Result.success(userId)
    }
}
