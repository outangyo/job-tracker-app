package com.mocode.jobtracker.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mocode.jobtracker.data.local.dao.ApplicationDao
import com.mocode.jobtracker.data.local.dao.TimelineEventDao
import com.mocode.jobtracker.data.local.entity.ApplicationEntity
import com.mocode.jobtracker.data.local.entity.TimelineEventEntity
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.domain.model.TimelineEventType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class TimelineEventDaoTest {

    private lateinit var database: JobTrackerDatabase
    private lateinit var applicationDao: ApplicationDao
    private lateinit var timelineEventDao: TimelineEventDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, JobTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        applicationDao = database.applicationDao()
        timelineEventDao = database.timelineEventDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryTimelineEventsByApplicationId() = runTest {
        val app = ApplicationEntity(
            companyName = "Microsoft",
            position = "Full Stack Engineer",
            appliedDate = "2026-10-01",
            status = ApplicationStatus.APPLIED
        )
        val appId = applicationDao.insert(app)

        val event1 = TimelineEventEntity(
            applicationId = appId,
            eventType = TimelineEventType.APPLIED,
            eventDate = "2026-10-01",
            note = "Submitted CV via LinkedIn",
            createdAt = 1000L
        )
        val event2 = TimelineEventEntity(
            applicationId = appId,
            eventType = TimelineEventType.HR_CONTACTED,
            eventDate = "2026-10-03",
            note = "HR called for screening",
            createdAt = 2000L
        )

        timelineEventDao.insert(event1)
        timelineEventDao.insert(event2)

        val events = timelineEventDao.getEventsForApplication(appId).first()
        assertEquals(2, events.size)
        assertEquals(TimelineEventType.APPLIED, events[0].eventType)
        assertEquals(TimelineEventType.HR_CONTACTED, events[1].eventType)
    }

    @Test
    fun deleteTimelineEvent_removesEventOnly() = runTest {
        val app = ApplicationEntity(
            companyName = "Netflix",
            position = "Senior Engineer",
            appliedDate = "2026-10-01"
        )
        val appId = applicationDao.insert(app)

        val event = TimelineEventEntity(
            applicationId = appId,
            eventType = TimelineEventType.CUSTOM,
            eventDate = "2026-10-02",
            note = "Sent portfolio sample"
        )
        val eventId = timelineEventDao.insert(event)

        val loadedEvents = timelineEventDao.getEventsForApplicationOnce(appId)
        assertEquals(1, loadedEvents.size)

        timelineEventDao.delete(loadedEvents[0])
        val afterDelete = timelineEventDao.getEventsForApplicationOnce(appId)
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun cascadeDelete_whenApplicationDeleted_removesAssociatedTimelineEvents() = runTest {
        // 1. Insert application
        val app = ApplicationEntity(
            companyName = "Meta",
            position = "Product Engineer",
            appliedDate = "2026-10-01"
        )
        val appId = applicationDao.insert(app)

        // 2. Insert timeline events linked to it
        timelineEventDao.insert(
            TimelineEventEntity(
                applicationId = appId,
                eventType = TimelineEventType.APPLIED,
                eventDate = "2026-10-01",
                note = "Referral from friend"
            )
        )
        timelineEventDao.insert(
            TimelineEventEntity(
                applicationId = appId,
                eventType = TimelineEventType.INTERVIEW_SCHEDULED,
                eventDate = "2026-10-05",
                note = "Screening interview scheduled"
            )
        )

        val beforeDeleteEvents = timelineEventDao.getEventsForApplicationOnce(appId)
        assertEquals(2, beforeDeleteEvents.size)

        // 3. Delete application
        applicationDao.deleteById(appId)

        // 4. Verify cascade deletion of timeline events
        val afterDeleteEvents = timelineEventDao.getEventsForApplicationOnce(appId)
        assertTrue(
            "Associated timeline events must be cascade deleted when the parent application is deleted",
            afterDeleteEvents.isEmpty()
        )
    }

    @Test
    fun insertAndQueryCustomEvent_storesAndRetrievesCustomTitle() = runTest {
        val app = ApplicationEntity(
            companyName = "Discord",
            position = "Senior Android Engineer",
            appliedDate = "2026-10-01"
        )
        val appId = applicationDao.insert(app)

        val customEvent = TimelineEventEntity(
            applicationId = appId,
            eventType = TimelineEventType.CUSTOM,
            customTitle = "Salary Negotiation",
            eventDate = "2026-10-10",
            note = "Discussed equity & base salary with recruiter"
        )
        val eventId = timelineEventDao.insert(customEvent)

        val retrieved = timelineEventDao.getEventsForApplicationOnce(appId)
        assertEquals(1, retrieved.size)
        assertEquals("Salary Negotiation", retrieved[0].customTitle)
        assertEquals(TimelineEventType.CUSTOM, retrieved[0].eventType)
        assertEquals("2026-10-10", retrieved[0].eventDate)
    }

    @Test
    fun updateTimelineEvent_updatesFieldsCorrectly() = runTest {
        val app = ApplicationEntity(
            companyName = "Stripe",
            position = "Software Engineer",
            appliedDate = "2026-10-01"
        )
        val appId = applicationDao.insert(app)

        val event = TimelineEventEntity(
            applicationId = appId,
            eventType = TimelineEventType.CUSTOM,
            customTitle = "Initial Takehome",
            eventDate = "2026-10-02",
            note = "Received assignment"
        )
        val eventId = timelineEventDao.insert(event)

        val loaded = timelineEventDao.getEventsForApplicationOnce(appId)[0]
        val updated = loaded.copy(
            customTitle = "Completed Takehome",
            eventDate = "2026-10-05",
            note = "Submitted assignment on GitHub"
        )
        timelineEventDao.update(updated)

        val afterUpdate = timelineEventDao.getEventsForApplicationOnce(appId)[0]
        assertEquals("Completed Takehome", afterUpdate.customTitle)
        assertEquals("2026-10-05", afterUpdate.eventDate)
        assertEquals("Submitted assignment on GitHub", afterUpdate.note)
    }

    @Test
    fun getAllTimelineEvents_returnsAllEventsOrderedByDateDesc() = runTest {
        val app1 = ApplicationEntity(companyName = "App1", position = "P1", appliedDate = "2026-10-01")
        val app2 = ApplicationEntity(companyName = "App2", position = "P2", appliedDate = "2026-10-01")
        val app1Id = applicationDao.insert(app1)
        val app2Id = applicationDao.insert(app2)

        timelineEventDao.insert(
            TimelineEventEntity(
                applicationId = app1Id,
                eventType = TimelineEventType.APPLIED,
                eventDate = "2026-10-01",
                createdAt = 1000L
            )
        )
        timelineEventDao.insert(
            TimelineEventEntity(
                applicationId = app2Id,
                eventType = TimelineEventType.OFFER_RECEIVED,
                eventDate = "2026-10-10",
                createdAt = 2000L
            )
        )
        timelineEventDao.insert(
            TimelineEventEntity(
                applicationId = app1Id,
                eventType = TimelineEventType.INTERVIEW_SCHEDULED,
                eventDate = "2026-10-05",
                createdAt = 1500L
            )
        )

        val allEvents = timelineEventDao.getAllTimelineEventsOnce()
        assertEquals(3, allEvents.size)
        // Ordered eventDate DESC: 2026-10-10, 2026-10-05, 2026-10-01
        assertEquals("2026-10-10", allEvents[0].eventDate)
        assertEquals("2026-10-05", allEvents[1].eventDate)
        assertEquals("2026-10-01", allEvents[2].eventDate)

        val allEventsFromFlow = timelineEventDao.getAllTimelineEvents().first()
        assertEquals(3, allEventsFromFlow.size)
        assertEquals("2026-10-10", allEventsFromFlow[0].eventDate)
    }
}
