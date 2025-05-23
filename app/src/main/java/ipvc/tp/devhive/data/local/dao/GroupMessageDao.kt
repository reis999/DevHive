package ipvc.tp.devhive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ipvc.tp.devhive.data.local.entity.GroupMessageEntity

@Dao
interface GroupMessageDao {
    @Query("SELECT * FROM group_messages WHERE studyGroupId = :studyGroupId ORDER BY createdAt ASC")
    fun getMessagesByStudyGroupId(studyGroupId: String): LiveData<List<GroupMessageEntity>>

    @Query("SELECT * FROM group_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): GroupMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: GroupMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<GroupMessageEntity>)

    @Update
    suspend fun updateMessage(message: GroupMessageEntity)

    @Query("DELETE FROM group_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM group_messages WHERE studyGroupId = :studyGroupId")
    suspend fun deleteMessagesByStudyGroupId(studyGroupId: String)

    @Query("SELECT * FROM group_messages WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedMessages(): List<GroupMessageEntity>

    @Query("UPDATE group_messages SET syncStatus = :status WHERE id = :messageId")
    suspend fun updateSyncStatus(messageId: String, status: String)
}
