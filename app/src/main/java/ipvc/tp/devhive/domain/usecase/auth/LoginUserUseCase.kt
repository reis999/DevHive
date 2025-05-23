package ipvc.tp.devhive.domain.usecase.auth

import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date

/**
 * Caso de uso para autenticar um usuário
 */
class LoginUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(email: String, password: String): Result<String> {
        // Validação de dados
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email e senha são obrigatórios"))
        }

        // Aqui seria implementada a lógica de autenticação com Firebase Auth
        // Por enquanto, retornamos um ID de usuário fictício para demonstração
        val userId = "user123"

        // Atualiza o status online e o último login
        userRepository.updateUserOnlineStatus(userId, true)

        return Result.success(userId)
    }
}
