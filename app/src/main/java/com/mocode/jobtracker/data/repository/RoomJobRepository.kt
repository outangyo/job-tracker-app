package com.mocode.jobtracker.data.repository

import com.mocode.jobtracker.data.local.dao.ApplicationDao
import com.mocode.jobtracker.data.local.dao.TimelineEventDao
import com.mocode.jobtracker.data.local.mapper.toDomain
import com.mocode.jobtracker.data.local.mapper.toEntity
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.TimelineEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomJobRepository(
    private val applicationDao: ApplicationDao,
    private val timelineEventDao: TimelineEventDao
) : JobRepository {

    override fun getAllApplications(): Flow<List<Application>> {
        return applicationDao.getAllApplications().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getApplicationById(id: Long): Flow<Application?> {
        return applicationDao.getApplicationById(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getApplicationByIdOnce(id: Long): Application? {
        return applicationDao.getApplicationByIdOnce(id)?.toDomain()
    }

    override suspend fun insertApplication(application: Application): Long {
        return applicationDao.insert(application.toEntity())
    }

    override suspend fun updateApplication(application: Application) {
        applicationDao.update(application.toEntity())
    }

    override suspend fun deleteApplication(application: Application) {
        applicationDao.delete(application.toEntity())
    }

    override suspend fun deleteApplicationById(id: Long) {
        applicationDao.deleteById(id)
    }

    override fun getTimelineEvents(applicationId: Long): Flow<List<TimelineEvent>> {
        return timelineEventDao.getEventsForApplication(applicationId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTimelineEventsOnce(applicationId: Long): List<TimelineEvent> {
        return timelineEventDao.getEventsForApplicationOnce(applicationId).map { it.toDomain() }
    }

    override suspend fun insertTimelineEvent(event: TimelineEvent): Long {
        return timelineEventDao.insert(event.toEntity())
    }

    override suspend fun updateTimelineEvent(event: TimelineEvent) {
        timelineEventDao.update(event.toEntity())
    }

    override suspend fun deleteTimelineEvent(event: TimelineEvent) {
        timelineEventDao.delete(event.toEntity())
    }

    override suspend fun deleteTimelineEventsForApplication(applicationId: Long) {
        timelineEventDao.deleteEventsForApplication(applicationId)
    }
}
