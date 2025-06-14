package ipvc.tp.devhive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ipvc.tp.devhive.data.local.entity.MaterialEntity
import ipvc.tp.devhive.data.util.SyncStatus

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials")
    fun getAllMaterials(): LiveData<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id = :materialId")
    suspend fun getMaterialById(materialId: String): MaterialEntity?

    @Query("SELECT * FROM materials WHERE id = :materialId")
    fun observeMaterialById(materialId: String): LiveData<MaterialEntity?>

    @Query("SELECT * FROM materials WHERE ownerUid = :userId")
    fun getMaterialsByUser(userId: String): LiveData<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE subject = :subject")
    fun getMaterialsBySubject(subject: String): LiveData<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE :userId IN (bookmarkedBy)")
    suspend fun getUserBookmarks(userId: String): LiveData<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<MaterialEntity>)

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    @Query("SELECT * FROM materials WHERE syncStatus != :syncStatus")
    suspend fun getUnsyncedMaterials(syncStatus: String = SyncStatus.SYNCED): List<MaterialEntity>

    @Query("UPDATE materials SET syncStatus = :newStatus WHERE id = :materialId")
    suspend fun updateSyncStatus(materialId: String, newStatus: String)

    @Query("DELETE FROM materials WHERE id = :materialId")
    suspend fun deleteMaterialById(materialId: String)

    @Query("SELECT * FROM materials WHERE bookmarked = 1")
    fun getBookmarkedMaterials(): LiveData<List<MaterialEntity>>
}
