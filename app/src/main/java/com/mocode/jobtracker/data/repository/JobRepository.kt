package com.mocode.jobtracker.data.repository

import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.TimelineEvent
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing repository interface contract for JobTracker.
 */
interface JobRepository {
    // Applications
    fun getAllApplications(): Flow<List<Application>>
    fun getApplicationById(id: Long): Flow<Application?>
    suspend fun getApplicationByIdOnce(id: Long): Application?
    suspend fun insertApplication(application: Application): Long
    suspend fun updateApplication(application: Application)
    suspend fun deleteApplication(application: Application)
    suspend fun deleteApplicationById(id: Long)

    // Timeline Events
    fun getTimelineEvents(applicationId: Long): Flow<List<TimelineEvent>>
    suspend fun getTimelineEventsOnce(applicationId: Long): List<TimelineEvent>
    suspend fun insertTimelineEvent(event: TimelineEvent): Long
    suspend fun updateTimelineEvent(event: TimelineEvent)
    suspend fun deleteTimelineEvent(event: TimelineEvent)
    suspend fun deleteTimelineEventsForApplication(applicationId: Long)
}
