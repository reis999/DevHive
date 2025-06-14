package ipvc.tp.devhive.domain.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.usecase.user.StatsAction

/**
 * Interface de repositório para operações relacionadas a utilizadores
 */
interface UserRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun getCurrentUser(): User?
    fun observeUserById(userId: String): LiveData<User?>
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(name: String, bio: String, institution: String, course: String, imageUri: Uri? = null): Result<User>
    suspend fun updateUserStats(userId: String, action: StatsAction): Result<Boolean>
    suspend fun deleteUser(userId: String): Result<Boolean>
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Result<Boolean>
    suspend fun syncPendingUsers()
}
