package ipvc.tp.devhive.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID.randomUUID
import ipvc.tp.devhive.domain.model.GroupMessage as DomainGroupMessage
import ipvc.tp.devhive.domain.model.MessageAttachment as DomainMessageAttachment
import ipvc.tp.devhive.domain.repository.StudyGroupRepository as DomainStudyGroupRepository

class StudyGroupRepository(
    private val studyGroupDao: StudyGroupDao,
    private val groupMessageDao: GroupMessageDao,
    private val studyGroupService: StudyGroupService,
    private val appScope: CoroutineScope,
    private val storage: FirebaseStorage,
    private val applicationContext: Context
) : DomainStudyGroupRepository {

    override suspend fun getStudyGroups(): LiveData<List<ipvc.tp.devhive.domain.model.StudyGroup>> {
        withContext(Dispatchers.IO) {
            val result = studyGroupService.getStudyGroups()
            if (result.isSuccess) {
                val groups = result.getOrThrow()
                val entities = groups.map { StudyGroupEntity.fromStudyGroup(it) }
                studyGroupDao.insertStudyGroups(entities)
            } else {
                Log.e(
                    "StudyGroupRepo",
                    "Falha ao buscar grupos da rede: ${result.exceptionOrNull()?.message}"
                )
            }
        }

        return studyGroupDao.getStudyGroups().map { entities ->
            entities.map { StudyGroupEntity.toStudyGroup(it).toDomainStudyGroup() }
        }
    }

    override fun getStudyGroupsByUser(userId: String): LiveData<List<ipvc.tp.devhive.domain.model.StudyGroup>> {
        appScope.launch {
            refreshStudyGroupsByUser(userId)
        }

        return studyGroupDao.getStudyGroupsByUser(userId).map { entities ->
            entities.map { StudyGroupEntity.toStudyGroup(it).toDomainStudyGroup() }
        }
    }

    private suspend fun refreshStudyGroupsByUser(userId: String) {
        withContext(Dispatchers.IO) {
            Log.d("StudyGroupRepo", "Iniciando refresh para userId: $userId")
            try {
                val result = studyGroupService.getStudyGroupsByUser(userId)

                if (result.isSuccess) {
                    val groups = result.getOrThrow()

                    if (groups.isNotEmpty()) {
                        val entities = groups.map {
                            val entity = StudyGroupEntity.fromStudyGroup(it)
                            Log.d(
                                "StudyGroupRepo",
                                "Mapeando StudyGroup (dados) ${it.id} para StudyGroupEntity: $entity"
                            )
                            entity
                        }
                        studyGroupDao.insertStudyGroups(entities)

                    } else {
                        Log.d(
                            "StudyGroupRepo",
                            "Rede retornou lista vazia (não deveria acontecer com o log do service)."
                        )
                    }
                } else {
                    Log.e(
                        "StudyGroupRepo",
                        "Falha ao buscar grupos da rede para userId $userId: ${result.exceptionOrNull()?.message}",
                        result.exceptionOrNull()
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "StudyGroupRepo",
                    "Exceção em refreshStudyGroupsByUser para userId $userId: ${e.message}",
                    e
                )
            }
        }
    }

    override suspend fun getStudyGroupById(groupId: String): ipvc.tp.devhive.domain.model.StudyGroup? {
        return withContext(Dispatchers.IO) {
            val localGroup = studyGroupDao.getStudyGroupById(groupId)

            if (localGroup != null) {
                StudyGroupEntity.toStudyGroup(localGroup).toDomainStudyGroup()
            } else {
                val remoteGroup = studyGroupService.getStudyGroupById(groupId)

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
                    val createdGroup = result.getOrThrow()
                    studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(createdGroup))
                    Result.success(createdGroup.toDomainStudyGroup())
                } else {
                    val groupEntity =
                        StudyGroupEntity.fromStudyGroup(dataStudyGroup, SyncStatus.PENDING_UPLOAD)
                    studyGroupDao.insertStudyGroup(groupEntity)
                    Result.success(studyGroup)
                }
            } catch (e: Exception) {
                val groupEntity = StudyGroupEntity.fromStudyGroup(
                    studyGroup.toDataStudyGroup(),
                    SyncStatus.PENDING_UPLOAD
                )
                studyGroupDao.insertStudyGroup(groupEntity)
                Result.failure(e)
            }
        }
    }

    override suspend fun updateStudyGroup(
        groupId: String,
        name: String,
        description: String,
        categories: List<String>,
        imageUri: Uri?
    ): Result<ipvc.tp.devhive.domain.model.StudyGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val existingStudyGroupEntity = studyGroupDao.getStudyGroupById(groupId)
                    ?: return@withContext Result.failure(Exception("Study group not found with id: $groupId"))

                val existingStudyGroup = StudyGroupEntity.toStudyGroup(existingStudyGroupEntity)

                var imageUrl = existingStudyGroup.imageUrl
                if (imageUri != null) {
                    val uploadResult = uploadGroupImage(groupId, imageUri)
                    if (uploadResult.isSuccess) {
                        imageUrl = uploadResult.getOrNull()!!
                    } else {
                        Log.e(
                            "StudyGroupRepo",
                            "Failed to upload image for group $groupId",
                            uploadResult.exceptionOrNull()
                        )
                    }
                }

                val updatedStudyGroup = existingStudyGroup.copy(
                    name = name,
                    description = description,
                    categories = categories,
                    imageUrl = imageUrl,
                    updatedAt = Timestamp.now()
                )

                // Tenta atualizar no Firestore
                val result = studyGroupService.updateStudyGroup(updatedStudyGroup)

                if (result.isSuccess) {
                    // Se sucesso, atualiza no banco local
                    studyGroupDao.insertStudyGroup(
                        StudyGroupEntity.fromStudyGroup(
                            updatedStudyGroup,
                            SyncStatus.SYNCED
                        )
                    )
                    Result.success(updatedStudyGroup.toDomainStudyGroup())
                } else {
                    // Se falhar, atualiza localmente com status pendente
                    val groupEntity = StudyGroupEntity.fromStudyGroup(
                        updatedStudyGroup,
                        SyncStatus.PENDING_UPDATE
                    )
                    studyGroupDao.insertStudyGroup(groupEntity)
                    Result.success(updatedStudyGroup.toDomainStudyGroup())
                }
            } catch (e: Exception) {
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

    override suspend fun sendGroupMessage(
        groupId: String,
        messageContent: String,
        senderId: String,
        senderName: String,
        senderImageUrl: String,
        attachmentUri: Uri?,
        originalAttachmentFileName: String?
    ): Result<DomainGroupMessage> {
        return withContext(Dispatchers.IO) {
            try {
                val messageId = randomUUID().toString()
                var uploadedAttachments = listOf<DomainMessageAttachment>()

                if (attachmentUri != null) {
                    val uploadResult = uploadMessageAttachment(groupId, messageId, attachmentUri, originalAttachmentFileName)
                    if (uploadResult.isSuccess) {
                        uploadedAttachments = listOf(uploadResult.getOrThrow())
                    } else {
                        return@withContext Result.failure(
                            uploadResult.exceptionOrNull() ?: Exception("Failed to upload attachment.")
                        )
                    }
                }

                val domainMessage = DomainGroupMessage(
                    id = messageId,
                    studyGroupId = groupId,
                    content = messageContent,
                    senderUid = senderId,
                    senderName = senderName,
                    senderImageUrl = senderImageUrl,
                    createdAt =Timestamp(java.util.Date()),
                    attachments = uploadedAttachments
                )

                val dataMessage = domainMessage.toDataGroupMessage()

                val serviceResult = studyGroupService.sendGroupMessage(groupId, dataMessage)

                if (serviceResult.isSuccess) {
                    val sentMessageData = serviceResult.getOrThrow()
                    groupMessageDao.insertMessage(GroupMessageEntity.fromGroupMessage(sentMessageData))
                    Result.success(sentMessageData.toDomainGroupMessage())
                } else {

                    val messageEntity = GroupMessageEntity.fromGroupMessage(dataMessage, SyncStatus.PENDING_UPLOAD)
                    groupMessageDao.insertMessage(messageEntity)
                    Result.failure(serviceResult.exceptionOrNull() ?: Exception("Failed to send message to service."))
                }
            } catch (e: Exception) {
                // Em caso de erro, salva localmente com status pendente (se aplicável)
                // ou apenas falha
                Log.e("StudyGroupRepo", "Exception in sendGroupMessage: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun removeMember(groupId: String, memberId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val remoteRemoveResult = studyGroupService.removeMember(groupId, memberId)

            if (remoteRemoveResult.isSuccess && remoteRemoveResult.getOrNull() == true) {
                try {
                    val updatedStudyGroupDataModel =
                        studyGroupService.getStudyGroupById(groupId)

                    if (updatedStudyGroupDataModel != null) {
                        val studyGroupEntity = mapDataModelToEntity(updatedStudyGroupDataModel)
                        studyGroupDao.insertOrUpdateStudyGroup(studyGroupEntity)
                        Result.success(Unit)
                    } else {
                        Log.w(
                            "StudyGroupRepo",
                            "Membro removido remotamente, mas falha ao buscar grupo atualizado do Firestore."
                        )
                        Result.success(Unit)
                    }
                } catch (e: Exception) {
                    Log.e(
                        "StudyGroupRepo",
                        "Erro ao sincronizar Room após remoção remota: ${e.message}",
                        e
                    )
                    Result.failure(
                        Exception(
                            "Membro removido remotamente, mas falha ao sincronizar dados locais.",
                            e
                        )
                    )
                }
            } else {
                Result.failure(
                    remoteRemoveResult.exceptionOrNull()
                        ?: Exception("Falha ao remover membro do serviço.")
                )
            }
        }
    }

    private suspend fun uploadGroupImage(groupId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "group_cover_${randomUUID()}.jpg"
                val imageRef = storage.reference.child("study_group_images/$groupId/$fileName")

                imageRef.putFile(imageUri).await()

                val downloadUrl = imageRef.downloadUrl.await().toString()
                Result.success(downloadUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun uploadMessageAttachment(
        groupId: String,
        messageId: String, // Para organizar no Storage
        attachmentUri: Uri,
        originalFileName: String? // Para tentar manter o nome original
    ): Result<DomainMessageAttachment> {
        return withContext(Dispatchers.IO) {
            try {
                val fileExtension = applicationContext.contentResolver.getType(attachmentUri)?.substringAfterLast('/')
                    ?: originalFileName?.substringAfterLast('.')
                    ?: "bin" // Extensão padrão

                val fileNameInStorage = "attachment_${randomUUID()}.$fileExtension"
                val attachmentRef = storage.reference.child("study_groups/$groupId/messages/$messageId/$fileNameInStorage")

                attachmentRef.putFile(attachmentUri).await()
                val downloadUrl = attachmentRef.downloadUrl.await().toString()

                // Obter metadados para tamanho, se possível, ou estimar
                val metadata = attachmentRef.metadata.await()
                val sizeBytes = metadata?.sizeBytes ?: 0L // Ou tente obter do URI antes do upload

                val displayName = originalFileName ?: fileNameInStorage

                Result.success(
                    DomainMessageAttachment(
                        id = randomUUID().toString(), // ID único para o objeto MessageAttachment
                        name = displayName,
                        type = applicationContext.contentResolver.getType(attachmentUri) ?: "application/octet-stream",
                        url = downloadUrl,
                        size = sizeBytes,
                        fileExtension = fileExtension
                    )
                )
            } catch (e: Exception) {
                Log.e("StudyGroupRepo", "Failed to upload message attachment", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun isUserAdmin(groupId: String, userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val studyGroup = studyGroupService.getStudyGroupById(groupId)
                studyGroup?.admins?.contains(userId) ?: false
            } catch (e: Exception) {
                false
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
                        val result =
                            studyGroupService.sendGroupMessage(messageEntity.studyGroupId, message)
                        if (result.isSuccess) {
                            groupMessageDao.updateSyncStatus(messageEntity.id, SyncStatus.SYNCED)
                        }
                    }
                }
            }
        }
    }

    override fun getPublicStudyGroups(): LiveData<List<ipvc.tp.devhive.domain.model.StudyGroup>> {
        appScope.launch {
            refreshPublicStudyGroups()
        }
        return studyGroupDao.getAllPublicStudyGroups().map { entities ->
            entities.map { StudyGroupEntity.toStudyGroup(it).toDomainStudyGroup() }
        }
    }

    private suspend fun refreshPublicStudyGroups() {
        withContext(Dispatchers.IO) {
            try {
                val result = studyGroupService.getAllPublicStudyGroups()
                if (result.isSuccess) {
                    val groups = result.getOrThrow()
                    val entities = groups.map { StudyGroupEntity.fromStudyGroup(it) }
                    studyGroupDao.insertStudyGroups(entities)
                } else {
                    Log.e(
                        "StudyGroupRepo",
                        "Failed to refresh public groups: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e("StudyGroupRepo", "Exception in refreshPublicStudyGroups: ${e.message}", e)
            }
        }
    }

    override suspend fun getStudyGroupByJoinCode(joinCode: String): Result<ipvc.tp.devhive.domain.model.StudyGroup?> {
        return withContext(Dispatchers.IO) {
            val localEntity = studyGroupDao.getStudyGroupByJoinCode(joinCode)
            if (localEntity != null && localEntity.isPrivate) {
                return@withContext Result.success(
                    StudyGroupEntity.toStudyGroup(localEntity).toDomainStudyGroup()
                )
            }

            val remoteResult = studyGroupService.getStudyGroupByJoinCode(joinCode)
            if (remoteResult.isSuccess) {
                val remoteGroup = remoteResult.getOrNull()
                remoteGroup?.let {
                    studyGroupDao.insertStudyGroup(StudyGroupEntity.fromStudyGroup(it))
                    Result.success(it.toDomainStudyGroup())
                } ?: Result.success(null)
            } else {
                Log.e(
                    "StudyGroupRepo",
                    "Failed to fetch group by join code from service",
                    remoteResult.exceptionOrNull()
                )
                Result.failure(
                    remoteResult.exceptionOrNull()
                        ?: Exception("Unknown error fetching group by join code")
                )
            }
        }
    }

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
            id = this.id,
            name = this.name,
            url = this.url,
            type = this.type,
            size = this.size,
            fileExtension = this.fileExtension
        )
    }

    private fun ipvc.tp.devhive.domain.model.MessageAttachment.toDataMessageAttachment(): MessageAttachment {
        return MessageAttachment(
            id = this.id,
            name = this.name,
            url = this.url,
            type = this.type,
            size = this.size,
            fileExtension = this.fileExtension
        )
    }

    private fun mapDataModelToEntity(dataModel: StudyGroup): StudyGroupEntity {
        return StudyGroupEntity.fromStudyGroup(dataModel)
    }
}

