package com.mocode.jobtracker.data.repository

import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.TimelineEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeJobRepository : JobRepository {

    private val applicationsFlow = MutableStateFlow<List<Application>>(emptyList())
    private val timelineEventsFlow = MutableStateFlow<List<TimelineEvent>>(emptyList())
    private var nextAppId = 1L
    private var nextEventId = 1L

    override fun getAllApplications(): Flow<List<Application>> = applicationsFlow

    override fun getApplicationById(id: Long): Flow<Application?> {
        return applicationsFlow.map { list -> list.firstOrNull { it.id == id } }
    }

    override suspend fun getApplicationByIdOnce(id: Long): Application? {
        return applicationsFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun insertApplication(application: Application): Long {
        val id = if (application.id > 0) application.id else nextAppId++
        val newApp = application.copy(id = id)
        applicationsFlow.update { current ->
            listOf(newApp) + current.filter { it.id != id }
        }
        return id
    }

    override suspend fun updateApplication(application: Application) {
        applicationsFlow.update { current ->
            current.map { if (it.id == application.id) application else it }
        }
    }

    override suspend fun deleteApplication(application: Application) {
        deleteApplicationById(application.id)
    }

    override suspend fun deleteApplicationById(id: Long) {
        applicationsFlow.update { current -> current.filter { it.id != id } }
        deleteTimelineEventsForApplication(id)
    }

    override fun getTimelineEvents(applicationId: Long): Flow<List<TimelineEvent>> {
        return timelineEventsFlow.map { list ->
            list.filter { it.applicationId == applicationId }
        }
    }

    override suspend fun getTimelineEventsOnce(applicationId: Long): List<TimelineEvent> {
        return timelineEventsFlow.value.filter { it.applicationId == applicationId }
    }

    override fun getAllTimelineEvents(): Flow<List<TimelineEvent>> = timelineEventsFlow

    override suspend fun getAllTimelineEventsOnce(): List<TimelineEvent> = timelineEventsFlow.value

    override suspend fun insertTimelineEvent(event: TimelineEvent): Long {
        val id = if (event.id > 0) event.id else nextEventId++
        val newEvent = event.copy(id = id)
        timelineEventsFlow.update { it + newEvent }
        return id
    }

    override suspend fun updateTimelineEvent(event: TimelineEvent) {
        timelineEventsFlow.update { current ->
            current.map { if (it.id == event.id) event else it }
        }
    }

    override suspend fun deleteTimelineEvent(event: TimelineEvent) {
        timelineEventsFlow.update { current -> current.filter { it.id != event.id } }
    }

    override suspend fun deleteTimelineEventsForApplication(applicationId: Long) {
        timelineEventsFlow.update { current -> current.filter { it.applicationId != applicationId } }
    }
}
