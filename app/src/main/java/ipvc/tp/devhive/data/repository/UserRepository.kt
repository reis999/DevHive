package ipvc.tp.devhive.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ipvc.tp.devhive.data.local.dao.UserDao
import ipvc.tp.devhive.data.local.entity.UserEntity
import ipvc.tp.devhive.data.model.User
import ipvc.tp.devhive.data.remote.service.UserService
import ipvc.tp.devhive.data.util.FirebaseAuthHelper
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ipvc.tp.devhive.domain.model.User as DomainUser
import ipvc.tp.devhive.domain.repository.UserRepository as DomainUserRepository


class UserRepository(
    private val userDao: UserDao,
    private val userService: UserService,
    private val appScope: CoroutineScope
) : DomainUserRepository {

    override suspend fun getUserById(userId: String): ipvc.tp.devhive.domain.model.User? {
        return withContext(Dispatchers.IO) {
            // Primeiro tenta obter do banco de dados local
            val localUser = userDao.getUserById(userId)

            if (localUser != null) {
                UserEntity.toUser(localUser).toDomainUser()
            } else {
                // Se não encontrar localmente, busca do Firestore
                val remoteUser = userService.getUserById(userId)

                // Se encontrar remotamente, salva no banco local
                if (remoteUser != null) {
                    userDao.insertUser(UserEntity.fromUser(remoteUser))
                    remoteUser.toDomainUser()
                } else {
                    null
                }
            }
        }
    }

    override suspend fun getCurrentUser(): DomainUser? {
        return withContext(Dispatchers.IO) {
            val currentFirebaseUserId = FirebaseAuthHelper.getCurrentUserId()
                ?: // Nenhum usuário Firebase logado
                return@withContext null

            // 1. Tenta obter do DAO local
            val localUserEntity = userDao.getUserById(currentFirebaseUserId)
            if (localUserEntity != null) {
                // Opcional: Você pode adicionar uma lógica para verificar se os dados locais estão "velhos"
                // e precisam ser atualizados do serviço antes de retornar.
                // Por agora, retornamos o local se existir.
                // Lança uma atualização em segundo plano para garantir que está atualizado para a próxima vez.
                appScope.launch { refreshUser(currentFirebaseUserId) }
                return@withContext UserEntity.toUser(localUserEntity).toDomainUser()
            }

            // 2. Se não estiver no DAO local, busca do UserService
            val remoteUser = userService.getUserById(currentFirebaseUserId) // userService.getCurrentUser() também funcionaria aqui
            remoteUser?.let { dataUser ->
                // Salva o usuário remoto no DAO local para cache
                userDao.insertUser(UserEntity.fromUser(dataUser, SyncStatus.SYNCED))
                return@withContext dataUser.toDomainUser()
            }

            // Se não encontrado localmente nem remotamente (ex: usuário deletado no backend mas ainda autenticado no Firebase?)
            return@withContext null
        }
    }

    override fun observeUserById(userId: String): LiveData<ipvc.tp.devhive.domain.model.User?> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshUser(userId)
        }

        // Retorna LiveData do banco local
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

    override suspend fun createUser(user: ipvc.tp.devhive.domain.model.User): Result<ipvc.tp.devhive.domain.model.User> {
        return withContext(Dispatchers.IO) {
            try {
                val dataUser = user.toDataUser()
                // Tenta criar no Firestore
                val result = userService.createUser(dataUser)

                if (result.isSuccess) {
                    // Se sucesso, salva no banco local
                    val createdUser = result.getOrThrow()
                    userDao.insertUser(UserEntity.fromUser(createdUser))
                    Result.success(createdUser.toDomainUser())
                } else {
                    // Se falhar, salva localmente com status pendente
                    val userEntity = UserEntity.fromUser(dataUser, SyncStatus.PENDING_UPLOAD)
                    userDao.insertUser(userEntity)
                    Result.success(user)
                }
            } catch (e: Exception) {
                // Em caso de erro, salva localmente com status pendente
                val userEntity = UserEntity.fromUser(user.toDataUser(), SyncStatus.PENDING_UPLOAD)
                userDao.insertUser(userEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUser(user: ipvc.tp.devhive.domain.model.User): Result<ipvc.tp.devhive.domain.model.User> {
        return withContext(Dispatchers.IO) {
            try {
                val dataUser = user.toDataUser()
                // Tenta atualizar no Firestore
                val result = userService.updateUser(dataUser)

                if (result.isSuccess) {
                    // Se sucesso, atualiza no banco local
                    userDao.insertUser(UserEntity.fromUser(dataUser))
                    Result.success(user)
                } else {
                    // Se falhar, atualiza localmente com status pendente
                    val userEntity = UserEntity.fromUser(dataUser, SyncStatus.PENDING_UPDATE)
                    userDao.insertUser(userEntity)
                    Result.success(user)
                }
            } catch (e: Exception) {
                // Em caso de erro, atualiza localmente com status pendente
                val userEntity = UserEntity.fromUser(user.toDataUser(), SyncStatus.PENDING_UPDATE)
                userDao.insertUser(userEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteUser(userId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Tenta deletar no Firestore
                val result = userService.deleteUser(userId)

                if (result.isSuccess) {
                    // Se sucesso, remove do banco local
                    userDao.deleteUserById(userId)
                    Result.success(true)
                } else {
                    // Se falhar, marca como pendente de deleção
                    val user = userDao.getUserById(userId)
                    if (user != null) {
                        val updatedUser = user.copy(syncStatus = SyncStatus.PENDING_DELETE)
                        userDao.updateUser(updatedUser)
                    }
                    Result.success(false)
                }
            } catch (e: Exception) {
                // Em caso de erro, marca como pendente de deleção
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
                    // Atualiza o status online no banco local
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
    private fun User.toDomainUser(): ipvc.tp.devhive.domain.model.User {
        return ipvc.tp.devhive.domain.model.User(
            id = this.id,
            name = this.name,
            username = this.username,
            email = this.email,
            profileImageUrl = this.profileImageUrl,
            course = this.course,
            institution = this.institution,
            bio = this.bio,
            isOnline = this.isOnline,
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

    private fun ipvc.tp.devhive.domain.model.User.toDataUser(): User {
        return User(
            id = this.id,
            name = this.name,
            username = this.username,
            email = this.email,
            profileImageUrl = this.profileImageUrl,
            course = this.course,
            institution = this.institution,
            bio = this.bio,
            isOnline = this.isOnline,
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
