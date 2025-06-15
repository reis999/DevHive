package ipvc.tp.devhive.domain.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.domain.model.StudyGroup

interface StudyGroupRepository {
    suspend fun getStudyGroups(): LiveData<List<StudyGroup>> 
    fun getStudyGroupsByUser(userId: String): LiveData<List<StudyGroup>> 
    fun getPublicStudyGroups(): LiveData<List<StudyGroup>> 
    suspend fun getStudyGroupById(groupId: String): StudyGroup? 
    suspend fun getStudyGroupByJoinCode(joinCode: String): Result<StudyGroup?>
    fun observeStudyGroupById(groupId: String): LiveData<StudyGroup?>

    suspend fun createStudyGroup(studyGroup: StudyGroup): Result<StudyGroup> 
    suspend fun updateStudyGroup(groupId: String, name: String, description: String, categories: List<String>, imageUri: Uri? = null): Result<StudyGroup>
    suspend fun deleteStudyGroup(groupId: String): Result<Boolean> 

    suspend fun joinStudyGroup(groupId: String, userId: String): Result<Boolean> 
    suspend fun leaveStudyGroup(groupId: String, userId: String): Result<Boolean> 

    fun getGroupMessagesByStudyGroupId(groupId: String): LiveData<List<GroupMessage>> 
    suspend fun sendGroupMessage(
        groupId: String,
        messageContent: String,
        senderId: String,
        senderName: String,
        senderImageUrl: String,
        attachmentUri: Uri?,
        originalAttachmentFileName: String?
    ): Result<GroupMessage> 

    suspend fun removeMember(groupId: String, memberId: String): Result<Unit> 
    suspend fun isUserAdmin(groupId: String, userId: String): Boolean 

    suspend fun syncPendingStudyGroups()
    suspend fun syncPendingGroupMessages()
}
