package ipvc.tp.devhive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
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

    @Query("""
        SELECT * FROM materials 
        WHERE (title LIKE '%' || :query || '%' 
               OR description LIKE '%' || :query || '%'
               OR subject LIKE '%' || :query || '%'
               OR ownerName LIKE '%' || :query || '%')
        AND syncStatus != 'PENDING_DELETE'
        ORDER BY createdAt DESC
    """)
    fun searchMaterials(query: String): LiveData<List<MaterialEntity>>

    @Query("SELECT DISTINCT subject FROM materials WHERE subject != '' AND syncStatus != 'PENDING_DELETE' ORDER BY subject ASC")
    suspend fun getDistinctSubjects(): List<String>

    @Query("""
        SELECT * FROM materials 
        WHERE subject = :subject 
        AND syncStatus != 'PENDING_DELETE'
        ORDER BY createdAt DESC
    """)
    fun getMaterialsBySubject(subject: String): LiveData<List<MaterialEntity>>

    @Query("""
    SELECT * FROM materials 
    WHERE (
        CASE 
            WHEN :query = '' THEN 1
            ELSE (
                title LIKE '%' || :query || '%' 
                OR description LIKE '%' || :query || '%'
                OR subject LIKE '%' || :query || '%'
                OR ownerName LIKE '%' || :query || '%'
            )
        END
    ) 
    AND (
        CASE 
            WHEN :subject IS NULL THEN 1
            ELSE subject = :subject
        END
    )
    AND syncStatus != 'PENDING_DELETE'
    ORDER BY createdAt DESC
""")
    fun searchMaterialsWithSubject(query: String, subject: String?): LiveData<List<MaterialEntity>>

}
