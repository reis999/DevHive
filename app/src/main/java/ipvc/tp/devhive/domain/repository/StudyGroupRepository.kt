package ipvc.tp.devhive.domain.repository

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.model.GroupMessage

/**
 * Interface de repositório para operações relacionadas a grupos de estudo e suas mensagens
 */
interface StudyGroupRepository {
    fun getStudyGroupsByUser(userId: String): LiveData<List<StudyGroup>>
    suspend fun getStudyGroupById(groupId: String): StudyGroup?
    fun observeStudyGroupById(groupId: String): LiveData<StudyGroup?>
    suspend fun createStudyGroup(studyGroup: StudyGroup): Result<StudyGroup>
    suspend fun updateStudyGroup(studyGroup: StudyGroup): Result<StudyGroup>
    suspend fun deleteStudyGroup(groupId: String): Result<Boolean>
    suspend fun joinStudyGroup(groupId: String, userId: String): Result<Boolean>
    suspend fun leaveStudyGroup(groupId: String, userId: String): Result<Boolean>
    fun getGroupMessagesByStudyGroupId(groupId: String): LiveData<List<GroupMessage>>
    suspend fun sendGroupMessage(groupId: String, message: GroupMessage): Result<GroupMessage>
    suspend fun syncPendingStudyGroups()
    suspend fun syncPendingGroupMessages()
}
