package com.mocode.jobtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mocode.jobtracker.data.local.entity.ApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {

    @Query("SELECT * FROM applications ORDER BY updatedAt DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE id = :id")
    fun getApplicationById(id: Long): Flow<ApplicationEntity?>

    @Query("SELECT * FROM applications WHERE id = :id")
    suspend fun getApplicationByIdOnce(id: Long): ApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: ApplicationEntity): Long

    @Update
    suspend fun update(application: ApplicationEntity)

    @Delete
    suspend fun delete(application: ApplicationEntity)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM applications")
    suspend fun getCount(): Int
}
