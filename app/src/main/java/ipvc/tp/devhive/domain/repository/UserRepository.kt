package ipvc.tp.devhive.domain.repository

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.User

/**
 * Interface de repositório para operações relacionadas a usuários
 */
interface UserRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun getCurrentUser(): User?
    fun observeUserById(userId: String): LiveData<User?>
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(userId: String): Result<Boolean>
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Result<Boolean>
    suspend fun syncPendingUsers()
}
