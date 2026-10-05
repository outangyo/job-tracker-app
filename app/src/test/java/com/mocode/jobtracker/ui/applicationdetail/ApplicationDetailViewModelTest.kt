package com.mocode.jobtracker.ui.applicationdetail

import androidx.lifecycle.SavedStateHandle
import com.mocode.jobtracker.data.repository.FakeJobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.domain.model.TimelineEvent
import com.mocode.jobtracker.domain.model.TimelineEventType
import com.mocode.jobtracker.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeJobRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeJobRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun detailViewModel_loadsApplicationAndTimelineEvents() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Grab",
                position = "Android Staff Engineer",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.INTERVIEW
            )
        )
        repository.insertTimelineEvent(
            TimelineEvent(
                applicationId = appId,
                eventType = TimelineEventType.APPLIED,
                eventDate = "2026-10-01",
                note = "Submitted"
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.application)
        assertEquals("Grab", state.application?.companyName)
        assertEquals(1, state.timelineEvents.size)
        assertEquals("Submitted", state.timelineEvents[0].note)
    }

    @Test
    fun detailViewModel_deleteConfirmationFlow_removesApplicationAndMarksDeleted() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "To Delete Corp",
                position = "Engineer",
                appliedDate = "2026-10-01"
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertFalse(viewModel.uiState.value.isDeleted)

        // User clicks delete icon
        viewModel.onDeleteClicked()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showDeleteDialog)

        // User cancels
        viewModel.onDismissDeleteDialog()
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showDeleteDialog)

        // User confirms delete
        viewModel.onDeleteClicked()
        viewModel.onConfirmDelete()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDeleted)
        assertNull(repository.getApplicationByIdOnce(appId))
    }

    @Test
    fun detailViewModel_withAllOptionalFieldsPopulated_loadsCorrectly() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Google",
                position = "Android Staff Engineer",
                jobUrl = "https://careers.google.com/jobs/123",
                location = "Bangkok, Thailand",
                salary = "200k THB",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.INTERVIEW,
                interviewDate = "2026-10-15",
                interviewTime = "14:00",
                interviewRound = "Technical",
                interviewType = "Online",
                interviewNotes = "Prepare system design",
                followUpDate = "2026-10-20",
                followUpNote = "Email recruiter",
                generalNotes = "Referred by John"
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val app = viewModel.uiState.value.application
        assertNotNull(app)
        assertEquals("Google", app?.companyName)
        assertEquals("https://careers.google.com/jobs/123", app?.jobUrl)
        assertEquals("Bangkok, Thailand", app?.location)
        assertEquals("200k THB", app?.salary)
        assertEquals("2026-10-15", app?.interviewDate)
        assertEquals("14:00", app?.interviewTime)
        assertEquals("Technical", app?.interviewRound)
        assertEquals("Online", app?.interviewType)
        assertEquals("Prepare system design", app?.interviewNotes)
        assertEquals("2026-10-20", app?.followUpDate)
        assertEquals("Email recruiter", app?.followUpNote)
        assertEquals("Referred by John", app?.generalNotes)
    }

    @Test
    fun detailViewModel_withOptionalFieldsEmpty_loadsCleanly() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Minimal Co",
                position = "Developer",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.APPLIED
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val app = viewModel.uiState.value.application
        assertNotNull(app)
        assertEquals("Minimal Co", app?.companyName)
        assertNull(app?.jobUrl)
        assertNull(app?.location)
        assertNull(app?.salary)
        assertNull(app?.interviewDate)
        assertNull(app?.interviewTime)
        assertNull(app?.interviewRound)
        assertNull(app?.interviewType)
        assertNull(app?.interviewNotes)
        assertNull(app?.followUpDate)
        assertNull(app?.followUpNote)
        assertNull(app?.generalNotes)
    }

    @Test
    fun timeline_addStandardEvent_persistsEventAndDoesNotChangeApplicationStatus() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Shopify",
                position = "Staff Android Developer",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.APPLIED
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        // User clicks add event
        viewModel.onAddEventClicked()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAddingEvent)
        assertNull(viewModel.uiState.value.eventBeingEdited)

        // Save event (Interview Completed)
        viewModel.saveTimelineEvent(
            eventType = TimelineEventType.INTERVIEW_COMPLETED,
            customTitle = null,
            eventDate = "2026-10-05",
            note = "Technical interview went very well"
        )
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAddingEvent)
        val events = repository.getTimelineEventsOnce(appId)
        assertEquals(1, events.size)
        assertEquals(TimelineEventType.INTERVIEW_COMPLETED, events[0].eventType)
        assertEquals("2026-10-05", events[0].eventDate)
        assertEquals("Technical interview went very well", events[0].note)

        // CRITICAL SEPARATION RULE: Manual timeline event must NOT change Application status
        val currentApp = repository.getApplicationByIdOnce(appId)
        assertEquals(ApplicationStatus.APPLIED, currentApp?.status)
        assertEquals(ApplicationStatus.APPLIED, viewModel.uiState.value.application?.status)
    }

    @Test
    fun timeline_addCustomEvent_persistsCustomTitleAndDoesNotChangeStatus() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Canva",
                position = "Frontend Engineer",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.WISHLIST
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onAddEventClicked()
        viewModel.saveTimelineEvent(
            eventType = TimelineEventType.CUSTOM,
            customTitle = "Take-home Assignment",
            eventDate = "2026-10-06",
            note = "Received 48-hour design project"
        )
        testScheduler.advanceUntilIdle()

        val events = repository.getTimelineEventsOnce(appId)
        assertEquals(1, events.size)
        assertEquals(TimelineEventType.CUSTOM, events[0].eventType)
        assertEquals("Take-home Assignment", events[0].customTitle)
        assertEquals("Take-home Assignment", events[0].displayTitle)
        assertEquals("Received 48-hour design project", events[0].note)

        // Application status remains WISHLIST
        val currentApp = repository.getApplicationByIdOnce(appId)
        assertEquals(ApplicationStatus.WISHLIST, currentApp?.status)
    }

    @Test
    fun timeline_editExistingEvent_updatesEventCorrectlyAndPreservesApplicationStatus() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Atlassian",
                position = "Kotlin Dev",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.INTERVIEW
            )
        )
        val initialEventId = repository.insertTimelineEvent(
            TimelineEvent(
                applicationId = appId,
                eventType = TimelineEventType.INTERVIEW_SCHEDULED,
                eventDate = "2026-10-10",
                note = "Initial date"
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val eventToEdit = viewModel.uiState.value.timelineEvents.first { it.id == initialEventId }
        viewModel.onEditEventClicked(eventToEdit)
        testScheduler.advanceUntilIdle()

        assertEquals(eventToEdit, viewModel.uiState.value.eventBeingEdited)
        assertFalse(viewModel.uiState.value.isAddingEvent)

        // Update event: change to Custom, reschedule date, update note
        viewModel.saveTimelineEvent(
            eventType = TimelineEventType.CUSTOM,
            customTitle = "Rescheduled Interview",
            eventDate = "2026-10-15",
            note = "Rescheduled due to recruiter conflict"
        )
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.eventBeingEdited)

        val updatedEvents = repository.getTimelineEventsOnce(appId)
        assertEquals(1, updatedEvents.size)
        assertEquals(TimelineEventType.CUSTOM, updatedEvents[0].eventType)
        assertEquals("Rescheduled Interview", updatedEvents[0].customTitle)
        assertEquals("2026-10-15", updatedEvents[0].eventDate)
        assertEquals("Rescheduled due to recruiter conflict", updatedEvents[0].note)

        // Status unchanged
        assertEquals(ApplicationStatus.INTERVIEW, repository.getApplicationByIdOnce(appId)?.status)
    }

    @Test
    fun timeline_deleteEvent_dialogFlow_deletesFromRepositoryAndPreservesStatus() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Oracle",
                position = "Backend Engineer",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.INTERVIEW
            )
        )
        val eventId = repository.insertTimelineEvent(
            TimelineEvent(
                applicationId = appId,
                eventType = TimelineEventType.FOLLOW_UP_SENT,
                eventDate = "2026-10-08",
                note = "Sent email"
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val event = viewModel.uiState.value.timelineEvents.first { it.id == eventId }

        // Open delete dialog
        viewModel.onDeleteEventClicked(event)
        testScheduler.advanceUntilIdle()
        assertEquals(event, viewModel.uiState.value.eventToDelete)

        // Dismiss delete dialog
        viewModel.onDismissDeleteEventDialog()
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.eventToDelete)

        // Click delete and confirm
        viewModel.onDeleteEventClicked(event)
        viewModel.onConfirmDeleteEvent()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.eventToDelete)
        val remainingEvents = repository.getTimelineEventsOnce(appId)
        assertTrue(remainingEvents.isEmpty())

        // Application status unchanged
        assertEquals(ApplicationStatus.INTERVIEW, repository.getApplicationByIdOnce(appId)?.status)
    }

    @Test
    fun timeline_eventsSortedChronologically_newestToOldest() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Discord",
                position = "Staff Engineer",
                appliedDate = "2026-10-01"
            )
        )

        // Insert events in non-chronological order
        repository.insertTimelineEvent(
            TimelineEvent(
                applicationId = appId,
                eventType = TimelineEventType.APPLIED,
                eventDate = "2026-10-01",
                note = "Applied",
                createdAt = 1000L
            )
        )
        repository.insertTimelineEvent(
            TimelineEvent(
                applicationId = appId,
                eventType = TimelineEventType.OFFER_RECEIVED,
                eventDate = "2026-10-20",
                note = "Offer received",
                createdAt = 3000L
            )
        )
        repository.insertTimelineEvent(
            TimelineEvent(
                applicationId = appId,
                eventType = TimelineEventType.INTERVIEW_COMPLETED,
                eventDate = "2026-10-10",
                note = "Interview done",
                createdAt = 2000L
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val eventsInUi = viewModel.uiState.value.timelineEvents
        assertEquals(3, eventsInUi.size)

        // Must be sorted Newest -> Oldest
        assertEquals("2026-10-20", eventsInUi[0].eventDate)
        assertEquals(TimelineEventType.OFFER_RECEIVED, eventsInUi[0].eventType)

        assertEquals("2026-10-10", eventsInUi[1].eventDate)
        assertEquals(TimelineEventType.INTERVIEW_COMPLETED, eventsInUi[1].eventType)

        assertEquals("2026-10-01", eventsInUi[2].eventDate)
        assertEquals(TimelineEventType.APPLIED, eventsInUi[2].eventType)
    }

    @Test
    fun timeline_dismissEventDialog_clearsBothAddAndEditStates() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Reddit",
                position = "Android Engineer",
                appliedDate = "2026-10-01"
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ApplicationDetail.ARG_APPLICATION_ID to appId))
        val viewModel = ApplicationDetailViewModel(repository, savedStateHandle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onAddEventClicked()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAddingEvent)

        viewModel.onDismissEventDialog()
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAddingEvent)
        assertNull(viewModel.uiState.value.eventBeingEdited)
    }
}


