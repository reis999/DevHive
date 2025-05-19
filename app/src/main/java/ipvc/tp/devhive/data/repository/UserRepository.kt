package ipvc.tp.devhive.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ipvc.tp.devhive.data.local.dao.UserDao
import ipvc.tp.devhive.data.local.entity.UserEntity
import ipvc.tp.devhive.data.model.User
import ipvc.tp.devhive.data.remote.service.UserService
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(
    private val userDao: UserDao,
    private val userService: UserService,
    private val appScope: CoroutineScope
) {
    suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) {
            val localUser = userDao.getUserById(userId)

            if (localUser != null) {
                UserEntity.toUser(localUser)
            } else {
                val remoteUser = userService.getUserById(userId)

                if (remoteUser != null) {
                    userDao.insertUser(UserEntity.fromUser(remoteUser))
                }

                remoteUser
            }
        }
    }

    fun observeUserById(userId: String): LiveData<User?> {
        appScope.launch {
            refreshUser(userId)
        }

        return userDao.observeUserById(userId).map { entity ->
            entity?.let { UserEntity.toUser(it) }
        }
    }

    private suspend fun refreshUser(userId: String) {
        withContext(Dispatchers.IO) {
            val remoteUser = userService.getUserById(userId)
            if (remoteUser != null) {
                userDao.insertUser(UserEntity.fromUser(remoteUser))
            }
        }
    }

    suspend fun createUser(user: User): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val result = userService.createUser(user)

                if (result.isSuccess) {
                    val createdUser = result.getOrThrow()
                    userDao.insertUser(UserEntity.fromUser(createdUser))
                    Result.success(createdUser)
                } else {
                    val userEntity = UserEntity.fromUser(user, SyncStatus.PENDING_UPLOAD)
                    userDao.insertUser(userEntity)
                    Result.success(user)
                }
            } catch (e: Exception) {
                val userEntity = UserEntity.fromUser(user, SyncStatus.PENDING_UPLOAD)
                userDao.insertUser(userEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun updateUser(user: User): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val result = userService.updateUser(user)

                if (result.isSuccess) {
                    userDao.insertUser(UserEntity.fromUser(user))
                    Result.success(user)
                } else {
                    val userEntity = UserEntity.fromUser(user, SyncStatus.PENDING_UPDATE)
                    userDao.insertUser(userEntity)
                    Result.success(user)
                }
            } catch (e: Exception) {
                val userEntity = UserEntity.fromUser(user, SyncStatus.PENDING_UPDATE)
                userDao.insertUser(userEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteUser(userId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = userService.deleteUser(userId)

                if (result.isSuccess) {
                    userDao.deleteUserById(userId)
                    Result.success(true)
                } else {
                    val user = userDao.getUserById(userId)
                    if (user != null) {
                        val updatedUser = user.copy(syncStatus = SyncStatus.PENDING_DELETE)
                        userDao.updateUser(updatedUser)
                    }
                    Result.success(false)
                }
            } catch (e: Exception) {
                val user = userDao.getUserById(userId)
                if (user != null) {
                    val updatedUser = user.copy(syncStatus = SyncStatus.PENDING_DELETE)
                    userDao.updateUser(updatedUser)
                }
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = userService.updateUserOnlineStatus(userId, isOnline)

                if (result.isSuccess) {
                    val user = userDao.getUserById(userId)
                    if (user != null) {
                        val updatedUser = user.copy(isOnline = isOnline)
                        userDao.updateUser(updatedUser)
                    }
                }

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun syncPendingUsers() {
        withContext(Dispatchers.IO) {
            val unsyncedUsers = userDao.getUnsyncedUsers()

            for (userEntity in unsyncedUsers) {
                when (userEntity.syncStatus) {
                    SyncStatus.PENDING_UPLOAD -> {
                        val user = UserEntity.toUser(userEntity)
                        val result = userService.createUser(user)
                        if (result.isSuccess) {
                            userDao.updateSyncStatus(userEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_UPDATE -> {
                        val user = UserEntity.toUser(userEntity)
                        val result = userService.updateUser(user)
                        if (result.isSuccess) {
                            userDao.updateSyncStatus(userEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_DELETE -> {
                        val result = userService.deleteUser(userEntity.id)
                        if (result.isSuccess) {
                            userDao.deleteUserById(userEntity.id)
                        }
                    }
                }
            }
        }
    }
}
