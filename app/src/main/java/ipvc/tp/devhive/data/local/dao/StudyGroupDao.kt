package ipvc.tp.devhive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ipvc.tp.devhive.data.local.entity.StudyGroupEntity

@Dao
interface StudyGroupDao {

    @Query("SELECT * FROM study_groups ORDER BY updatedAt DESC")
    fun getStudyGroups(): LiveData<List<StudyGroupEntity>>

    @Query("SELECT * FROM study_groups WHERE members LIKE '%' || :userId || '%' ORDER BY updatedAt DESC")
    fun getStudyGroupsByUser(userId: String): LiveData<List<StudyGroupEntity>>

    @Query("SELECT * FROM study_groups WHERE id = :studyGroupId")
    suspend fun getStudyGroupById(studyGroupId: String): StudyGroupEntity?

    @Query("SELECT * FROM study_groups WHERE id = :studyGroupId")
    fun observeStudyGroupById(studyGroupId: String): LiveData<StudyGroupEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGroup(studyGroup: StudyGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGroups(studyGroups: List<StudyGroupEntity>)

    @Update
    suspend fun updateStudyGroup(studyGroup: StudyGroupEntity)

    @Query("DELETE FROM study_groups WHERE id = :studyGroupId")
    suspend fun deleteStudyGroupById(studyGroupId: String)

    @Query("SELECT * FROM study_groups WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedStudyGroups(): List<StudyGroupEntity>

    @Query("UPDATE study_groups SET syncStatus = :status WHERE id = :studyGroupId")
    suspend fun updateSyncStatus(studyGroupId: String, status: String)

    @Query("SELECT * FROM study_groups WHERE isPrivate = 0 AND name LIKE '%' || :query || '%'")
    suspend fun searchPublicStudyGroups(query: String): List<StudyGroupEntity>

    @Query("SELECT * FROM study_groups WHERE isPrivate = 0 ORDER BY name ASC")
    fun getAllPublicStudyGroups(): LiveData<List<StudyGroupEntity>>

    @Query("SELECT * FROM study_groups WHERE joinCode = :joinCode LIMIT 1")
    suspend fun getStudyGroupByJoinCode(joinCode: String): StudyGroupEntity?
}
