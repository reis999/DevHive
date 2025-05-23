package ipvc.tp.devhive.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ipvc.tp.devhive.data.local.dao.GroupMessageDao
import ipvc.tp.devhive.data.local.dao.StudyGroupDao
import ipvc.tp.devhive.data.local.entity.GroupMessageEntity
import ipvc.tp.devhive.data.local.entity.StudyGroupEntity
import ipvc.tp.devhive.data.model.GroupMessage
import ipvc.tp.devhive.data.model.StudyGroup
import ipvc.tp.devhive.data.remote.service.StudyGroupService
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudyGroupRepository(
    private val studyGroupDao: StudyGroupDao,
    private val groupMessageDao: GroupMessageDao,
    private val studyGroupService: StudyGroupService,
    private val appScope: CoroutineScope
) {
    fun getStudyGroupsByUser(userId: String): LiveData<List<StudyGroup>> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshStudyGroupsByUser(userId)
        }

        // Retorna LiveData do banco local
        return studyGroupDao.getStudyGroupsByUser(userId).map { entities ->
            entities.map { StudyGroupEntity.toStudyGroup(it) }
        }
    }

    private suspend fun refreshStudyGroupsByUser(userId: String) {
        withContext(Dispatchers.IO) {
            val result = studyGroupService.getStudyGroupsByUser(userId)
            if (result.isSuccess) {
                val groups = result.getOrThrow()
                val entities = groups.map { StudyGroupEntity.fromStudyGroup(it) }
                studyGroupDao.insertStudyGroups(entities)
            }
        }
    }

    suspend fun getStudyGroupById(groupId: String): StudyGroup? {
        return withContext(Dispatchers.IO) {
            // Primeiro tenta obter do banco de dados local
            val localGroup = studyGroupDao.getStudyGroupById(groupId)

            if (localGroup != null) {
                StudyGroupEntity.toStudyGroup(localGroup)
            } else {
                // Se não encontrar localmente, busca do Firestore
                val remoteGroup = studyGroupService.getStudyGroupById(groupId)

                // Se encontrar remotamente, salva no banco local
                if (remoteGroup != null) {
                    studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(remoteGroup))
                }

                remoteGroup
            }
        }
    }

    fun observeStudyGroupById(groupId: String): LiveData<StudyGroup?> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshStudyGroup(groupId)
        }

        // Retorna LiveData do banco local
        return studyGroupDao.observeStudyGroupById(groupId).map { entity ->
            entity?.let { StudyGroupEntity.toStudyGroup(it) }
        }
    }

    private suspend fun refreshStudyGroup(groupId: String) {
        withContext(Dispatchers.IO) {
            val remoteGroup = studyGroupService.getStudyGroupById(groupId)
            if (remoteGroup != null) {
                studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(remoteGroup))
            }
        }
    }

    suspend fun createStudyGroup(studyGroup: StudyGroup): Result<StudyGroup> {
        return withContext(Dispatchers.IO) {
            try {
                // Tenta criar no Firestore
                val result = studyGroupService.createStudyGroup(studyGroup)

                if (result.isSuccess) {
                    // Se sucesso, salva no banco local
                    val createdGroup = result.getOrThrow()
                    studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(createdGroup))
                    Result.success(createdGroup)
                } else {
                    // Se falhar, salva localmente com status pendente
                    val groupEntity = StudyGroupEntity.fromStudyGroup(studyGroup, SyncStatus.PENDING_UPLOAD)
                    studyGroupDao.insertStudyGroup(groupEntity)
                    Result.success(studyGroup)
                }
            } catch (e: Exception) {
                // Em caso de erro, salva localmente com status pendente
                val groupEntity = StudyGroupEntity.fromStudyGroup(studyGroup, SyncStatus.PENDING_UPLOAD)
                studyGroupDao.insertStudyGroup(groupEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun updateStudyGroup(studyGroup: StudyGroup): Result<StudyGroup> {
        return withContext(Dispatchers.IO) {
            try {
                // Tenta atualizar no Firestore
                val result = studyGroupService.updateStudyGroup(studyGroup)

                if (result.isSuccess) {
                    // Se sucesso, atualiza no banco local
                    studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(studyGroup))
                    Result.success(studyGroup)
                } else {
                    // Se falhar, atualiza localmente com status pendente
                    val groupEntity = StudyGroupEntity.fromStudyGroup(studyGroup, SyncStatus.PENDING_UPDATE)
                    studyGroupDao.insertStudyGroup(groupEntity)
                    Result.success(studyGroup)
                }
            } catch (e: Exception) {
                // Em caso de erro, atualiza localmente com status pendente
                val groupEntity = StudyGroupEntity.fromStudyGroup(studyGroup, SyncStatus.PENDING_UPDATE)
                studyGroupDao.insertStudyGroup(groupEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteStudyGroup(groupId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Tenta deletar no Firestore
                val result = studyGroupService.deleteStudyGroup(groupId)

                if (result.isSuccess) {
                    // Se sucesso, remove do banco local
                    studyGroupDao.deleteStudyGroupById(groupId)
                    Result.success(true)
                } else {
                    // Se falhar, marca como pendente de deleção
                    val group = studyGroupDao.getStudyGroupById(groupId)
                    if (group != null) {
                        val updatedGroup = group.copy(syncStatus = SyncStatus.PENDING_DELETE)
                        studyGroupDao.updateStudyGroup(updatedGroup)
                    }
                    Result.success(false)
                }
            } catch (e: Exception) {
                // Em caso de erro, marca como pendente de deleção
                val group = studyGroupDao.getStudyGroupById(groupId)
                if (group != null) {
                    val updatedGroup = group.copy(syncStatus = SyncStatus.PENDING_DELETE)
                    studyGroupDao.updateStudyGroup(updatedGroup)
                }
                Result.failure(e)
            }
        }
    }

    suspend fun joinStudyGroup(groupId: String, userId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = studyGroupService.addMember(groupId, userId)

                if (result.isSuccess) {
                    // Atualiza a lista de membros localmente
                    val group = studyGroupDao.getStudyGroupById(groupId)
                    if (group != null) {
                        val updatedMembers = group.members.toMutableList()
                        if (!updatedMembers.contains(userId)) {
                            updatedMembers.add(userId)
                        }
                        val updatedGroup = group.copy(members = updatedMembers)
                        studyGroupDao.updateStudyGroup(updatedGroup)
                    }
                }

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun leaveStudyGroup(groupId: String, userId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = studyGroupService.removeMember(groupId, userId)

                if (result.isSuccess) {
                    // Atualiza a lista de membros localmente
                    val group = studyGroupDao.getStudyGroupById(groupId)
                    if (group != null) {
                        val updatedMembers = group.members.toMutableList()
                        updatedMembers.remove(userId)
                        val updatedGroup = group.copy(members = updatedMembers)
                        studyGroupDao.updateStudyGroup(updatedGroup)
                    }
                }

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getGroupMessagesByStudyGroupId(groupId: String): LiveData<List<GroupMessage>> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshGroupMessagesByStudyGroupId(groupId)
        }

        // Retorna LiveData do banco local
        return groupMessageDao.getMessagesByStudyGroupId(groupId).map { entities ->
            entities.map { GroupMessageEntity.toGroupMessage(it) }
        }
    }

    private suspend fun refreshGroupMessagesByStudyGroupId(groupId: String) {
        withContext(Dispatchers.IO) {
            val result = studyGroupService.getGroupMessages(groupId)
            if (result.isSuccess) {
                val messages = result.getOrThrow()
                val entities = messages.map { GroupMessageEntity.fromGroupMessage(it) }
                groupMessageDao.insertMessages(entities)
            }
        }
    }

    suspend fun sendGroupMessage(groupId: String, message: GroupMessage): Result<GroupMessage> {
        return withContext(Dispatchers.IO) {
            try {
                // Tenta enviar para o Firestore
                val result = studyGroupService.sendGroupMessage(groupId, message)

                if (result.isSuccess) {
                    // Se sucesso, salva no banco local
                    val sentMessage = result.getOrThrow()
                    groupMessageDao.insertMessage(GroupMessageEntity.fromGroupMessage(sentMessage))
                    Result.success(sentMessage)
                } else {
                    // Se falhar, salva localmente com status pendente
                    val messageEntity = GroupMessageEntity.fromGroupMessage(message, SyncStatus.PENDING_UPLOAD)
                    groupMessageDao.insertMessage(messageEntity)
                    Result.success(message)
                }
            } catch (e: Exception) {
                // Em caso de erro, salva localmente com status pendente
                val messageEntity = GroupMessageEntity.fromGroupMessage(message, SyncStatus.PENDING_UPLOAD)
                groupMessageDao.insertMessage(messageEntity)
                Result.failure(e)
            }
        }
    }

    suspend fun syncPendingStudyGroups() {
        withContext(Dispatchers.IO) {
            val unsyncedGroups = studyGroupDao.getUnsyncedStudyGroups()

            for (groupEntity in unsyncedGroups) {
                when (groupEntity.syncStatus) {
                    SyncStatus.PENDING_UPLOAD -> {
                        val group = StudyGroupEntity.toStudyGroup(groupEntity)
                        val result = studyGroupService.createStudyGroup(group)
                        if (result.isSuccess) {
                            studyGroupDao.updateSyncStatus(groupEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_UPDATE -> {
                        val group = StudyGroupEntity.toStudyGroup(groupEntity)
                        val result = studyGroupService.updateStudyGroup(group)
                        if (result.isSuccess) {
                            studyGroupDao.updateSyncStatus(groupEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_DELETE -> {
                        val result = studyGroupService.deleteStudyGroup(groupEntity.id)
                        if (result.isSuccess) {
                            studyGroupDao.deleteStudyGroupById(groupEntity.id)
                        }
                    }
                }
            }
        }
    }

    suspend fun syncPendingGroupMessages() {
        withContext(Dispatchers.IO) {
            val unsyncedMessages = groupMessageDao.getUnsyncedMessages()

            for (messageEntity in unsyncedMessages) {
                when (messageEntity.syncStatus) {
                    SyncStatus.PENDING_UPLOAD -> {
                        val message = GroupMessageEntity.toGroupMessage(messageEntity)
                        val result = studyGroupService.sendGroupMessage(messageEntity.studyGroupId, message)
                        if (result.isSuccess) {
                            groupMessageDao.updateSyncStatus(messageEntity.id, SyncStatus.SYNCED)
                        }
                    }
                    // Mensagens geralmente não são atualizadas ou excluídas, mas poderíamos implementar se necessário
                }
            }
        }
    }
}
