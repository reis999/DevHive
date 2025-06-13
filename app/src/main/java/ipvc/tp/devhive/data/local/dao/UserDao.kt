package ipvc.tp.devhive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ipvc.tp.devhive.data.local.entity.UserEntity
import ipvc.tp.devhive.data.util.SyncStatus

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<String>): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: String): LiveData<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE syncStatus != :syncStatus")
    suspend fun getUnsyncedUsers(syncStatus: String = SyncStatus.SYNCED): List<UserEntity>

    @Query("UPDATE users SET syncStatus = :newStatus WHERE id = :userId")
    suspend fun updateSyncStatus(userId: String, newStatus: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
}
