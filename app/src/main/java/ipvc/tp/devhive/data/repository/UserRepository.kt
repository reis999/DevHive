package ipvc.tp.devhive.data.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.storage.FirebaseStorage
import ipvc.tp.devhive.data.local.dao.UserDao
import ipvc.tp.devhive.data.local.entity.UserEntity
import ipvc.tp.devhive.data.model.User
import ipvc.tp.devhive.data.remote.service.UserService
import ipvc.tp.devhive.data.util.FirebaseAuthHelper
import ipvc.tp.devhive.data.util.SyncStatus
import ipvc.tp.devhive.domain.usecase.user.StatsAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID.randomUUID
import ipvc.tp.devhive.domain.model.User as DomainUser
import ipvc.tp.devhive.domain.repository.UserRepository as DomainUserRepository


class UserRepository(
    private val userDao: UserDao,
    private val userService: UserService,
    private val storage: FirebaseStorage,
    private val appScope: CoroutineScope
) : DomainUserRepository {

    override suspend fun getUserById(userId: String): DomainUser? {
        return withContext(Dispatchers.IO) {
            val localUser = userDao.getUserById(userId)

            if (localUser != null) {
                UserEntity.toUser(localUser).toDomainUser()
            } else {
                val remoteUser = userService.getUserById(userId)

                if (remoteUser != null) {
                    userDao.insertUser(UserEntity.fromUser(remoteUser))
                    remoteUser.toDomainUser()
                } else {
                    null
                }
            }
        }
    }

    override suspend fun getUsersByIds(userIds: List<String>): Result<List<ipvc.tp.devhive.domain.model.User>> {
        return withContext(Dispatchers.IO) {
            try {
                if (userIds.isEmpty()) {
                    return@withContext Result.success(emptyList())
                }

                val localUsers = userDao.getUsersByIds(userIds)
                val localUserMap = localUsers.associateBy { it.id }

                val missingUserIds = userIds.filter { userId ->
                    !localUserMap.containsKey(userId)
                }

                if (missingUserIds.isEmpty()) {
                    val domainUsers = localUsers.map { UserEntity.toUser(it).toDomainUser() }
                    return@withContext Result.success(domainUsers)
                }

                val remoteResult = userService.getUsersByIds(missingUserIds)

                if (remoteResult.isFailure) {
                    val availableUsers = localUsers.map { UserEntity.toUser(it).toDomainUser() }
                    return@withContext Result.success(availableUsers)
                }

                val remoteUsers = remoteResult.getOrThrow()

                if (remoteUsers.isNotEmpty()) {
                    val remoteUserEntities = remoteUsers.map { user ->
                        UserEntity.fromUser(user, SyncStatus.SYNCED)
                    }
                    userDao.insertUsers(remoteUserEntities)
                }

                val allUsers = mutableListOf<ipvc.tp.devhive.domain.model.User>()
                allUsers.addAll(localUsers.map { UserEntity.toUser(it).toDomainUser() })
                allUsers.addAll(remoteUsers.map { it.toDomainUser() })

                val userMap = allUsers.associateBy { it.id }
                val orderedUsers = userIds.mapNotNull { userId ->
                    userMap[userId]
                }

                Result.success(orderedUsers)

            } catch (e: Exception) {
                try {
                    val localUsers = userDao.getUsersByIds(userIds)
                    val domainUsers = localUsers.map { UserEntity.toUser(it).toDomainUser() }
                    Result.success(domainUsers)
                } catch (localException: Exception) {
                    Result.failure(e)
                }
            }
        }
    }


    override suspend fun getCurrentUser(): DomainUser? {
        return withContext(Dispatchers.IO) {
            val currentFirebaseUserId = FirebaseAuthHelper.getCurrentUserId()
                ?:
                return@withContext null

            // DAO local
            val localUserEntity = userDao.getUserById(currentFirebaseUserId)
            if (localUserEntity != null) {
                appScope.launch { refreshUser(currentFirebaseUserId) }
                return@withContext UserEntity.toUser(localUserEntity).toDomainUser()
            }

            val remoteUser = userService.getUserById(currentFirebaseUserId)
            remoteUser?.let { dataUser ->
                userDao.insertUser(UserEntity.fromUser(dataUser, SyncStatus.SYNCED))
                return@withContext dataUser.toDomainUser()
            }

            return@withContext null
        }
    }

    override suspend fun searchUsers(
        query: String,
        excludeUserId: String?
    ): Result<List<DomainUser>> {
        return withContext(Dispatchers.IO) {
            try {
                val result = userService.searchUsers(query, excludeUserId)
                return@withContext Result.success(result.map { it.toDomainUser() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun observeUserById(userId: String): LiveData<DomainUser?> {
        appScope.launch {
            refreshUser(userId)
        }

        return userDao.observeUserById(userId).map { entity ->
            entity?.let { UserEntity.toUser(it).toDomainUser() }
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

    override suspend fun createUser(user: DomainUser): Result<DomainUser> {
        return withContext(Dispatchers.IO) {
            try {
                val dataUser = user.toDataUser()
                val result = userService.createUser(dataUser)

                if (result.isSuccess) {
                    val createdUser = result.getOrThrow()
                    userDao.insertUser(UserEntity.fromUser(createdUser))
                    Result.success(createdUser.toDomainUser())
                } else {
                    val userEntity = UserEntity.fromUser(dataUser, SyncStatus.PENDING_UPLOAD)
                    userDao.insertUser(userEntity)
                    Result.success(user)
                }
            } catch (e: Exception) {
                val userEntity = UserEntity.fromUser(user.toDataUser(), SyncStatus.PENDING_UPLOAD)
                userDao.insertUser(userEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUser(
        name: String,
        bio: String,
        institution: String,
        course: String,
        imageUri: Uri?
    ): Result<ipvc.tp.devhive.domain.model.User> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = getCurrentUser()
                    ?: return@withContext Result.failure(Exception("Utilizador não encontrado"))

                var profileImageUrl = currentUser.profileImageUrl
                if (imageUri != null) {
                    val uploadResult = uploadProfileImage(currentUser.id, imageUri)
                    if (uploadResult.isSuccess) {
                        profileImageUrl = uploadResult.getOrNull()!!
                    } else {
                        Log.e(
                            "UserRepository",
                            "Failed to upload profile image for user ${currentUser.id}",
                            uploadResult.exceptionOrNull()
                        )
                    }
                }

                val updatedUser = currentUser.copy(
                    name = name,
                    bio = bio,
                    institution = institution,
                    course = course,
                    profileImageUrl = profileImageUrl
                ).toDataUser()

                val result = userService.updateUser(updatedUser)
                if (result.isSuccess) {
                    userDao.insertUser(
                        UserEntity.fromUser(
                            updatedUser,
                            SyncStatus.SYNCED
                        )
                    )
                    Result.success(updatedUser.toDomainUser())
                } else {
                    val userEntity = UserEntity.fromUser(
                        updatedUser,
                        SyncStatus.PENDING_UPDATE
                    )
                    userDao.insertUser(userEntity)
                    Result.success(updatedUser.toDomainUser())
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "profile_${randomUUID()}.jpg"
                val imageRef = storage.reference.child("profile_images/$userId/$fileName")

                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                Result.success(downloadUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUserStats(userId: String, action: StatsAction): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val user = getUserById(userId)
                    ?: return@withContext Result.failure(Exception("Utilizador não encontrado"))

                val updatedStats = when (action) {
                    StatsAction.INCREMENT_COMMENTS -> user.contributionStats.copy(
                        comments = user.contributionStats.comments + 1
                    )
                    StatsAction.INCREMENT_LIKES -> user.contributionStats.copy(
                        likes = user.contributionStats.likes + 1
                    )
                    StatsAction.INCREMENT_MATERIALS -> user.contributionStats.copy(
                        materials = user.contributionStats.materials + 1
                    )
                    StatsAction.INCREMENT_SESSIONS -> user.contributionStats.copy(
                        sessions = user.contributionStats.sessions + 1
                    )
                    StatsAction.DECREMENT_MATERIALS -> user.contributionStats.copy(
                        materials = (user.contributionStats.materials - 1).coerceAtLeast(0)
                    )
                    StatsAction.DECREMENT_LIKES -> user.contributionStats.copy(
                        likes = (user.contributionStats.likes - 1).coerceAtLeast(0)
                    )
                    StatsAction.DECREMENT_COMMENTS -> user.contributionStats.copy(
                        comments = (user.contributionStats.comments - 1).coerceAtLeast(0)
                    )
                }

                val updatedUser = user.copy(contributionStats = updatedStats).toDataUser()

                val result = userService.updateUser(updatedUser)
                if (result.isSuccess) {
                    userDao.insertUser(UserEntity.fromUser(updatedUser, SyncStatus.SYNCED))
                    Result.success(true)
                } else {
                    val userEntity = UserEntity.fromUser(updatedUser, SyncStatus.PENDING_UPDATE)
                    userDao.insertUser(userEntity)
                    Result.success(true)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteUser(userId: String): Result<Boolean> {
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

    override suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Result<Boolean> {
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

    override suspend fun syncPendingUsers() {
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

    // Funções de extensão para converter entre modelos data e domain
    private fun User.toDomainUser(): DomainUser {
        return ipvc.tp.devhive.domain.model.User(
            id = this.id,
            name = this.name,
            username = this.username,
            email = this.email,
            profileImageUrl = this.profileImageUrl,
            course = this.course,
            institution = this.institution,
            bio = this.bio,
            isOnline = this.online,
            createdAt = this.createdAt,
            lastLogin = this.lastLogin,
            contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                materials = this.contributionStats.materials,
                comments = this.contributionStats.comments,
                likes = this.contributionStats.likes,
                sessions = this.contributionStats.sessions
            )
        )
    }

    private fun DomainUser.toDataUser(): User {
        return User(
            id = this.id,
            name = this.name,
            username = this.username,
            email = this.email,
            profileImageUrl = this.profileImageUrl,
            course = this.course,
            institution = this.institution,
            bio = this.bio,
            online = this.isOnline,
            createdAt = this.createdAt,
            lastLogin = this.lastLogin,
            contributionStats = ipvc.tp.devhive.data.model.ContributionStats(
                materials = this.contributionStats.materials,
                comments = this.contributionStats.comments,
                likes = this.contributionStats.likes,
                sessions = this.contributionStats.sessions
            )
        )
    }
}
