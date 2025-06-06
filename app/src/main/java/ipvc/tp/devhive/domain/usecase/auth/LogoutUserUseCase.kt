package ipvc.tp.devhive.domain.usecase.auth

import ipvc.tp.devhive.domain.repository.AuthRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        // Atualiza o status online
        val statusResult = userRepository.updateUserOnlineStatus(userId, false)
        if (statusResult.isFailure) return statusResult

        // Faz signOut no Firebase
        return authRepository.logout()
    }
}
