package com.mocode.jobtracker.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mocode.jobtracker.data.local.JobTrackerDatabase
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.domain.model.TimelineEvent
import com.mocode.jobtracker.domain.model.TimelineEventType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class RoomJobRepositoryTest {

    private lateinit var database: JobTrackerDatabase
    private lateinit var repository: RoomJobRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, JobTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomJobRepository(
            applicationDao = database.applicationDao(),
            timelineEventDao = database.timelineEventDao()
        )
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun repository_insertAndQueryApplication() = runTest {
        val app = Application(
            companyName = "Twitter/X",
            position = "Senior Mobile Engineer",
            appliedDate = "2026-10-04",
            status = ApplicationStatus.WISHLIST
        )

        val id = repository.insertApplication(app)
        val loaded = repository.getApplicationByIdOnce(id)

        assertNotNull(loaded)
        assertEquals("Twitter/X", loaded?.companyName)
        assertEquals(ApplicationStatus.WISHLIST, loaded?.status)
    }

    @Test
    fun repository_observeAllApplicationsFlow() = runTest {
        repository.insertApplication(
            Application(
                companyName = "Uber",
                position = "Android Engineer",
                appliedDate = "2026-10-01"
            )
        )
        repository.insertApplication(
            Application(
                companyName = "Lyft",
                position = "Android Engineer",
                appliedDate = "2026-10-02"
            )
        )

        val applications = repository.getAllApplications().first()
        assertEquals(2, applications.size)
    }

    @Test
    fun repository_updateApplication() = runTest {
        val app = Application(
            companyName = "Spotify",
            position = "Android Developer",
            appliedDate = "2026-10-01",
            status = ApplicationStatus.APPLIED
        )
        val id = repository.insertApplication(app)

        val updatedApp = app.copy(
            id = id,
            status = ApplicationStatus.OFFER,
            salary = "140,000 THB"
        )
        repository.updateApplication(updatedApp)

        val loaded = repository.getApplicationByIdOnce(id)
        assertEquals(ApplicationStatus.OFFER, loaded?.status)
        assertEquals("140,000 THB", loaded?.salary)
    }

    @Test
    fun repository_deleteApplication() = runTest {
        val app = Application(
            companyName = "Airbnb",
            position = "Full Stack",
            appliedDate = "2026-10-01"
        )
        val id = repository.insertApplication(app)
        val loaded = repository.getApplicationByIdOnce(id)!!

        repository.deleteApplication(loaded)
        val afterDelete = repository.getApplicationByIdOnce(id)
        assertNull(afterDelete)
    }

    @Test
    fun repository_timelineEventsOperations() = runTest {
        val appId = repository.insertApplication(
            Application(
                companyName = "Apple",
                position = "Swift/Kotlin Engineer",
                appliedDate = "2026-10-01"
            )
        )

        val event = TimelineEvent(
            applicationId = appId,
            eventType = TimelineEventType.INTERVIEW_SCHEDULED,
            eventDate = "2026-10-07",
            note = "Technical phone screen"
        )
        val eventId = repository.insertTimelineEvent(event)

        val events = repository.getTimelineEvents(appId).first()
        assertEquals(1, events.size)
        assertEquals("Technical phone screen", events[0].note)

        repository.deleteTimelineEvent(events[0])
        val afterDelete = repository.getTimelineEventsOnce(appId)
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun repository_updateTimelineEvent() = runTest {
        val appId = repository.insertApplication(
            Application(
                companyName = "Figma",
                position = "UI Engineer",
                appliedDate = "2026-10-01"
            )
        )

        val event = TimelineEvent(
            applicationId = appId,
            eventType = TimelineEventType.CUSTOM,
            customTitle = "Portfolio Review",
            eventDate = "2026-10-05",
            note = "Initial submission"
        )
        val eventId = repository.insertTimelineEvent(event)

        val loaded = repository.getTimelineEventsOnce(appId)[0]
        val updated = loaded.copy(
            customTitle = "Design System Interview",
            note = "Walkthrough with staff designer"
        )
        repository.updateTimelineEvent(updated)

        val afterUpdate = repository.getTimelineEventsOnce(appId)[0]
        assertEquals("Design System Interview", afterUpdate.customTitle)
        assertEquals("Walkthrough with staff designer", afterUpdate.note)
    }
}
