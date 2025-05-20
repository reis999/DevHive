package ipvc.tp.devhive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ipvc.tp.devhive.data.local.entity.ChatEntity
import ipvc.tp.devhive.data.util.SyncStatus

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE :userId IN (participantIds)")
    fun getChatsByUser(userId: String): LiveData<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChatById(chatId: String): LiveData<ChatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)

    @Query("SELECT * FROM chats WHERE syncStatus != :syncStatus")
    suspend fun getUnsyncedChats(syncStatus: String = SyncStatus.SYNCED): List<ChatEntity>

    @Query("UPDATE chats SET syncStatus = :newStatus WHERE id = :chatId")
    suspend fun updateSyncStatus(chatId: String, newStatus: String)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)
}
