package com.mocode.jobtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mocode.jobtracker.data.local.entity.TimelineEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineEventDao {

    @Query("SELECT * FROM timeline_events WHERE applicationId = :applicationId ORDER BY createdAt ASC")
    fun getEventsForApplication(applicationId: Long): Flow<List<TimelineEventEntity>>

    @Query("SELECT * FROM timeline_events WHERE applicationId = :applicationId ORDER BY createdAt ASC")
    suspend fun getEventsForApplicationOnce(applicationId: Long): List<TimelineEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: TimelineEventEntity): Long

    @Update
    suspend fun update(event: TimelineEventEntity)

    @Delete
    suspend fun delete(event: TimelineEventEntity)

    @Query("DELETE FROM timeline_events WHERE applicationId = :applicationId")
    suspend fun deleteEventsForApplication(applicationId: Long)
}
