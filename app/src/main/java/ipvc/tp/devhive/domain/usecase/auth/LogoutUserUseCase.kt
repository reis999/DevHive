package ipvc.tp.devhive.domain.usecase.auth

import ipvc.tp.devhive.domain.repository.UserRepository

/**
 * Caso de uso para fazer logout de um usuário
 */
class LogoutUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(userId: String): Result<Boolean> {
        // Atualiza o status online para false
        return userRepository.updateUserOnlineStatus(userId, false)
    }
}
