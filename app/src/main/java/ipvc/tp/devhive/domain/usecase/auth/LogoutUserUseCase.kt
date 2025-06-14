package ipvc.tp.devhive.domain.usecase.auth

import android.util.Log
import ipvc.tp.devhive.domain.repository.AuthRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        Log.d("LogoutUseCase", "Attempting to update user $userId online status to false")
        val statusResult = userRepository.updateUserOnlineStatus(userId, false)
        if (statusResult.isFailure) {
            Log.e("LogoutUseCase", "Failed to update user online status: ${statusResult.exceptionOrNull()?.message}")
        } else {
            Log.d("LogoutUseCase", "User online status updated successfully.")
        }

        Log.d("LogoutUseCase", "Attempting Firebase signOut")
        val firebaseLogoutResult = authRepository.logout()
        if (firebaseLogoutResult.isSuccess) {
            Log.d("LogoutUseCase", "Firebase signOut successful.")
        } else {
            Log.e("LogoutUseCase", "Firebase signOut failed: ${firebaseLogoutResult.exceptionOrNull()?.message}")
        }
        return firebaseLogoutResult
    }
}
