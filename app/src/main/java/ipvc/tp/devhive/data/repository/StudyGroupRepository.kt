package ipvc.tp.devhive.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ipvc.tp.devhive.data.local.dao.GroupMessageDao
import ipvc.tp.devhive.data.local.dao.StudyGroupDao
import ipvc.tp.devhive.data.local.entity.GroupMessageEntity
import ipvc.tp.devhive.data.local.entity.StudyGroupEntity
import ipvc.tp.devhive.data.model.GroupMessage
import ipvc.tp.devhive.data.model.MessageAttachment
import ipvc.tp.devhive.data.model.StudyGroup
import ipvc.tp.devhive.data.remote.service.StudyGroupService
import ipvc.tp.devhive.data.util.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ipvc.tp.devhive.domain.repository.StudyGroupRepository as DomainStudyGroupRepository

class StudyGroupRepository(
    private val studyGroupDao: StudyGroupDao,
    private val groupMessageDao: GroupMessageDao,
    private val studyGroupService: StudyGroupService,
    private val appScope: CoroutineScope
) : DomainStudyGroupRepository {

    override fun getStudyGroupsByUser(userId: String): LiveData<List<ipvc.tp.devhive.domain.model.StudyGroup>> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshStudyGroupsByUser(userId)
        }

        // Retorna LiveData do banco local
        return studyGroupDao.getStudyGroupsByUser(userId).map { entities ->
            entities.map { StudyGroupEntity.toStudyGroup(it).toDomainStudyGroup() }
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

    override suspend fun getStudyGroupById(groupId: String): ipvc.tp.devhive.domain.model.StudyGroup? {
        return withContext(Dispatchers.IO) {
            // Primeiro tenta obter do banco de dados local
            val localGroup = studyGroupDao.getStudyGroupById(groupId)

            if (localGroup != null) {
                StudyGroupEntity.toStudyGroup(localGroup).toDomainStudyGroup()
            } else {
                // Se não encontrar localmente, busca do Firestore
                val remoteGroup = studyGroupService.getStudyGroupById(groupId)

                // Se encontrar remotamente, salva no banco local
                if (remoteGroup != null) {
                    studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(remoteGroup))
                    remoteGroup.toDomainStudyGroup()
                } else {
                    null
                }
            }
        }
    }

    override fun observeStudyGroupById(groupId: String): LiveData<ipvc.tp.devhive.domain.model.StudyGroup?> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshStudyGroup(groupId)
        }

        // Retorna LiveData do banco local
        return studyGroupDao.observeStudyGroupById(groupId).map { entity ->
            entity?.let { StudyGroupEntity.toStudyGroup(it).toDomainStudyGroup() }
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

    override suspend fun createStudyGroup(studyGroup: ipvc.tp.devhive.domain.model.StudyGroup): Result<ipvc.tp.devhive.domain.model.StudyGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val dataStudyGroup = studyGroup.toDataStudyGroup()
                // Tenta criar no Firestore
                val result = studyGroupService.createStudyGroup(dataStudyGroup)

                if (result.isSuccess) {
                    // Se sucesso, salva no banco local
                    val createdGroup = result.getOrThrow()
                    studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(createdGroup))
                    Result.success(createdGroup.toDomainStudyGroup())
                } else {
                    // Se falhar, salva localmente com status pendente
                    val groupEntity = StudyGroupEntity.fromStudyGroup(dataStudyGroup, SyncStatus.PENDING_UPLOAD)
                    studyGroupDao.insertStudyGroup(groupEntity)
                    Result.success(studyGroup)
                }
            } catch (e: Exception) {
                // Em caso de erro, salva localmente com status pendente
                val groupEntity = StudyGroupEntity.fromStudyGroup(studyGroup.toDataStudyGroup(), SyncStatus.PENDING_UPLOAD)
                studyGroupDao.insertStudyGroup(groupEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun updateStudyGroup(studyGroup: ipvc.tp.devhive.domain.model.StudyGroup): Result<ipvc.tp.devhive.domain.model.StudyGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val dataStudyGroup = studyGroup.toDataStudyGroup()
                // Tenta atualizar no Firestore
                val result = studyGroupService.updateStudyGroup(dataStudyGroup)

                if (result.isSuccess) {
                    // Se sucesso, atualiza no banco local
                    studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(dataStudyGroup))
                    Result.success(studyGroup)
                } else {
                    // Se falhar, atualiza localmente com status pendente
                    val groupEntity = StudyGroupEntity.fromStudyGroup(dataStudyGroup, SyncStatus.PENDING_UPDATE)
                    studyGroupDao.insertStudyGroup(groupEntity)
                    Result.success(studyGroup)
                }
            } catch (e: Exception) {
                // Em caso de erro, atualiza localmente com status pendente
                val groupEntity = StudyGroupEntity.fromStudyGroup(studyGroup.toDataStudyGroup(), SyncStatus.PENDING_UPDATE)
                studyGroupDao.insertStudyGroup(groupEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteStudyGroup(groupId: String): Result<Boolean> {
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

    override suspend fun joinStudyGroup(groupId: String, userId: String): Result<Boolean> {
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

    override suspend fun leaveStudyGroup(groupId: String, userId: String): Result<Boolean> {
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

    override fun getGroupMessagesByStudyGroupId(groupId: String): LiveData<List<ipvc.tp.devhive.domain.model.GroupMessage>> {
        // Busca do Firestore para atualizar o cache local
        appScope.launch {
            refreshGroupMessagesByStudyGroupId(groupId)
        }

        // Retorna LiveData do banco local
        return groupMessageDao.getMessagesByStudyGroupId(groupId).map { entities ->
            entities.map { GroupMessageEntity.toGroupMessage(it).toDomainGroupMessage() }
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

    override suspend fun sendGroupMessage(groupId: String, message: ipvc.tp.devhive.domain.model.GroupMessage): Result<ipvc.tp.devhive.domain.model.GroupMessage> {
        return withContext(Dispatchers.IO) {
            try {
                val dataMessage = message.toDataGroupMessage()
                // Tenta enviar para o Firestore
                val result = studyGroupService.sendGroupMessage(groupId, dataMessage)

                if (result.isSuccess) {
                    // Se sucesso, salva no banco local
                    val sentMessage = result.getOrThrow()
                    groupMessageDao.insertMessage(GroupMessageEntity.fromGroupMessage(sentMessage))
                    Result.success(sentMessage.toDomainGroupMessage())
                } else {
                    // Se falhar, salva localmente com status pendente
                    val messageEntity = GroupMessageEntity.fromGroupMessage(dataMessage, SyncStatus.PENDING_UPLOAD)
                    groupMessageDao.insertMessage(messageEntity)
                    Result.success(message)
                }
            } catch (e: Exception) {
                // Em caso de erro, salva localmente com status pendente
                val messageEntity = GroupMessageEntity.fromGroupMessage(message.toDataGroupMessage(), SyncStatus.PENDING_UPLOAD)
                groupMessageDao.insertMessage(messageEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun syncPendingStudyGroups() {
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

    override suspend fun syncPendingGroupMessages() {
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

    // Funções de extensão para converter entre modelos data e domain
    private fun StudyGroup.toDomainStudyGroup(): ipvc.tp.devhive.domain.model.StudyGroup {
        return ipvc.tp.devhive.domain.model.StudyGroup(
            id = this.id,
            name = this.name,
            description = this.description,
            imageUrl = this.imageUrl,
            createdBy = this.createdBy,
            createdAt = this.createdAt,
            members = this.members,
            maxMembers = this.maxMembers,
            isPrivate = this.isPrivate,
            updatedAt = this.updatedAt,
            admins = this.admins,
            categories = this.categories,
            joinCode = this.joinCode
        )
    }

    private fun ipvc.tp.devhive.domain.model.StudyGroup.toDataStudyGroup(): StudyGroup {
        return StudyGroup(
            id = this.id,
            name = this.name,
            description = this.description,
            imageUrl = this.imageUrl,
            createdBy = this.createdBy,
            createdAt = this.createdAt,
            members = this.members,
            maxMembers = this.maxMembers,
            isPrivate = this.isPrivate,
            updatedAt = this.updatedAt,
            admins = this.admins,
            categories = this.categories,
            joinCode = this.joinCode
        )
    }

    private fun GroupMessage.toDomainGroupMessage(): ipvc.tp.devhive.domain.model.GroupMessage {
        return ipvc.tp.devhive.domain.model.GroupMessage(
            id = this.id,
            studyGroupId = this.studyGroupId,
            senderUid = this.senderUid,
            senderName = this.senderName,
            senderImageUrl = this.senderImageUrl,
            content = this.content,
            createdAt = this.createdAt,
            replyToMessageId = this.replyToMessageId,
            isEdited = this.isEdited,
            attachments = this.attachments.map { it.toDomainMessageAttachment() },
            editedAt = this.editedAt
        )
    }

    private fun ipvc.tp.devhive.domain.model.GroupMessage.toDataGroupMessage(): GroupMessage {
        return GroupMessage(
            id = this.id,
            studyGroupId = this.studyGroupId,
            senderUid = this.senderUid,
            senderName = this.senderName,
            senderImageUrl = this.senderImageUrl,
            content = this.content,
            createdAt = this.createdAt,
            replyToMessageId = this.replyToMessageId,
            isEdited = this.isEdited,
            attachments = this.attachments.map { it.toDataMessageAttachment() },
            editedAt = this.editedAt
        )
    }

    private fun MessageAttachment.toDomainMessageAttachment(): ipvc.tp.devhive.domain.model.MessageAttachment {
        return ipvc.tp.devhive.domain.model.MessageAttachment(
            url = this.url,
            type = this.type,
            name = this.name,
            size = this.size
        )
    }

    // Função para converter domain.model.MessageAttachment para data.model.MessageAttachment
    private fun ipvc.tp.devhive.domain.model.MessageAttachment.toDataMessageAttachment(): MessageAttachment {
        return MessageAttachment(
            url = this.url,
            type = this.type,
            name = this.name,
            size = this.size
        )
    }
}

