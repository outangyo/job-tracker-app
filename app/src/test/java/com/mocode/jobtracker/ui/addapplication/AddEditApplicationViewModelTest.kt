package com.mocode.jobtracker.ui.addapplication

import androidx.lifecycle.SavedStateHandle
import com.mocode.jobtracker.data.repository.FakeJobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.domain.model.TimelineEvent
import com.mocode.jobtracker.domain.model.TimelineEventType
import com.mocode.jobtracker.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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
class AddEditApplicationViewModelTest {

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
    fun saveApplication_whenRequiredFieldsBlank_failsValidationWithErrors() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditApplicationViewModel(repository, savedStateHandle)

        viewModel.onCompanyNameChanged("")
        viewModel.onPositionChanged("")
        viewModel.saveApplication()

        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertEquals("Company name is required.", state.companyNameError)
        assertEquals("Position is required.", state.positionError)
    }

    @Test
    fun saveApplication_inAddMode_persistsApplicationAndInitialTimelineEvent() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditApplicationViewModel(repository, savedStateHandle)

        viewModel.onCompanyNameChanged("LINE MAN Wongnai")
        viewModel.onPositionChanged("Android Software Engineer")
        viewModel.onAppliedDateChanged("2026-10-04")
        viewModel.onStatusChanged(ApplicationStatus.APPLIED)
        viewModel.onInterviewDateChanged("2026-10-12")
        viewModel.onInterviewRoundChanged("Technical")
        viewModel.saveApplication()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)

        val applications = repository.getAllApplications().first()
        assertEquals(1, applications.size)
        assertEquals("LINE MAN Wongnai", applications[0].companyName)
        assertEquals("Android Software Engineer", applications[0].position)

        val events = repository.getTimelineEvents(applications[0].id).first()
        assertEquals(2, events.size)
        assertEquals(TimelineEventType.APPLIED, events[0].eventType)
        assertEquals(TimelineEventType.INTERVIEW_SCHEDULED, events[1].eventType)
    }

    @Test
    fun editApplication_loadsExistingDataAndUpdatesSuccessfully() = runTest(testDispatcher) {
        val originalAppId = repository.insertApplication(
            Application(
                companyName = "Initial Company",
                position = "Developer",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.APPLIED
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf(Screen.AddApplication.ARG_APPLICATION_ID to originalAppId))
        val viewModel = AddEditApplicationViewModel(repository, savedStateHandle)
        testScheduler.advanceUntilIdle()

        val loadedState = viewModel.uiState.value
        assertTrue(loadedState.isEditMode)
        assertEquals("Initial Company", loadedState.companyName)

        viewModel.onCompanyNameChanged("Updated Company")
        viewModel.onStatusChanged(ApplicationStatus.INTERVIEW)
        viewModel.saveApplication()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)

        val updated = repository.getApplicationByIdOnce(originalAppId)
        assertNotNull(updated)
        assertEquals("Updated Company", updated?.companyName)
        assertEquals(ApplicationStatus.INTERVIEW, updated?.status)

        // Status change timeline event recorded
        val events = repository.getTimelineEvents(originalAppId).first()
        assertTrue(events.any { it.eventType == TimelineEventType.INTERVIEW_SCHEDULED })
    }

    @Test
    fun inputFields_truncateWhenExceedingMaxLength() = runTest(testDispatcher) {
        val viewModel = AddEditApplicationViewModel(repository, SavedStateHandle())

        // Company Name max: 100
        val longCompany = "A".repeat(150)
        viewModel.onCompanyNameChanged(longCompany)
        assertEquals(100, viewModel.uiState.value.companyName.length)

        // Position max: 150
        val longPosition = "B".repeat(200)
        viewModel.onPositionChanged(longPosition)
        assertEquals(150, viewModel.uiState.value.position.length)

        // Location max: 150
        val longLocation = "C".repeat(200)
        viewModel.onLocationChanged(longLocation)
        assertEquals(150, viewModel.uiState.value.location.length)

        // Salary max: 100
        val longSalary = "D".repeat(120)
        viewModel.onSalaryChanged(longSalary)
        assertEquals(100, viewModel.uiState.value.salary.length)

        // Job URL max: 500
        val longUrl = "https://example.com/" + "x".repeat(600)
        viewModel.onJobUrlChanged(longUrl)
        assertEquals(500, viewModel.uiState.value.jobUrl.length)

        // Interview Notes max: 1000
        val longInterviewNotes = "E".repeat(1200)
        viewModel.onInterviewNotesChanged(longInterviewNotes)
        assertEquals(1000, viewModel.uiState.value.interviewNotes.length)

        // Follow-up Note max: 1000
        val longFollowUpNote = "F".repeat(1200)
        viewModel.onFollowUpNoteChanged(longFollowUpNote)
        assertEquals(1000, viewModel.uiState.value.followUpNote.length)

        // General Notes max: 2000
        val longGeneralNotes = "G".repeat(2500)
        viewModel.onGeneralNotesChanged(longGeneralNotes)
        assertEquals(2000, viewModel.uiState.value.generalNotes.length)
    }

    @Test
    fun dateChangeHandlers_updateStateCorrectly() = runTest(testDispatcher) {
        val viewModel = AddEditApplicationViewModel(repository, SavedStateHandle())

        viewModel.onAppliedDateChanged("2026-10-10")
        assertEquals("2026-10-10", viewModel.uiState.value.appliedDate)

        viewModel.onInterviewDateChanged("2026-10-15")
        assertEquals("2026-10-15", viewModel.uiState.value.interviewDate)

        viewModel.onFollowUpDateChanged("2026-10-20")
        assertEquals("2026-10-20", viewModel.uiState.value.followUpDate)

        // Clear optional dates
        viewModel.onInterviewDateChanged("")
        assertEquals("", viewModel.uiState.value.interviewDate)

        viewModel.onFollowUpDateChanged("")
        assertEquals("", viewModel.uiState.value.followUpDate)
    }

    @Test
    fun saveApplication_whenAppliedDateBlank_failsValidation() = runTest(testDispatcher) {
        val viewModel = AddEditApplicationViewModel(repository, SavedStateHandle())
        viewModel.onCompanyNameChanged("Valid Company")
        viewModel.onPositionChanged("Valid Position")
        viewModel.onAppliedDateChanged("")
        viewModel.saveApplication()

        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals("Applied date is required.", viewModel.uiState.value.appliedDateError)
    }

    @Test
    fun saveApplication_withOptionalFieldsEmpty_savesSuccessfully() = runTest(testDispatcher) {
        val viewModel = AddEditApplicationViewModel(repository, SavedStateHandle())
        viewModel.onCompanyNameChanged("Minimal Corp")
        viewModel.onPositionChanged("Developer")
        viewModel.onAppliedDateChanged("2026-10-01")
        viewModel.saveApplication()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        val apps = repository.getAllApplications().first()
        assertEquals(1, apps.size)
        assertEquals("Minimal Corp", apps[0].companyName)
        assertNull(apps[0].interviewDate)
        assertNull(apps[0].followUpDate)
        assertNull(apps[0].jobUrl)

        // Only 1 initial APPLIED event
        val events = repository.getTimelineEvents(apps[0].id).first()
        assertEquals(1, events.size)
        assertEquals(TimelineEventType.APPLIED, events[0].eventType)
    }

    @Test
    fun saveApplication_withInterviewAndFollowUp_inAddMode_persistsAllFieldsAndEvents() = runTest(testDispatcher) {
        val viewModel = AddEditApplicationViewModel(repository, SavedStateHandle())
        viewModel.onCompanyNameChanged("Agoda")
        viewModel.onPositionChanged("Staff Engineer")
        viewModel.onAppliedDateChanged("2026-10-01")
        viewModel.onInterviewDateChanged("2026-10-10")
        viewModel.onInterviewTimeChanged("10:00")
        viewModel.onInterviewRoundChanged("Technical")
        viewModel.onInterviewTypeChanged("Online")
        viewModel.onInterviewNotesChanged("System design preparation")
        viewModel.onFollowUpDateChanged("2026-10-15")
        viewModel.onFollowUpNoteChanged("Check status if no news")
        viewModel.saveApplication()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        val apps = repository.getAllApplications().first()
        assertEquals(1, apps.size)
        val app = apps[0]
        assertEquals("Agoda", app.companyName)
        assertEquals("2026-10-10", app.interviewDate)
        assertEquals("10:00", app.interviewTime)
        assertEquals("Technical", app.interviewRound)
        assertEquals("Online", app.interviewType)
        assertEquals("System design preparation", app.interviewNotes)
        assertEquals("2026-10-15", app.followUpDate)
        assertEquals("Check status if no news", app.followUpNote)

        // Events: APPLIED, INTERVIEW_SCHEDULED, FOLLOW_UP_SENT
        val events = repository.getTimelineEvents(app.id).first()
        assertEquals(3, events.size)
        assertEquals(TimelineEventType.APPLIED, events[0].eventType)
        assertEquals(TimelineEventType.INTERVIEW_SCHEDULED, events[1].eventType)
        assertEquals(TimelineEventType.FOLLOW_UP_SENT, events[2].eventType)
    }

    @Test
    fun editApplication_editingUnrelatedFields_doesNotDuplicateInterviewOrFollowUpEvents() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Initial Corp",
                position = "Dev",
                appliedDate = "2026-10-01",
                interviewDate = "2026-10-12",
                interviewRound = "HR",
                followUpDate = "2026-10-16",
                followUpNote = "Send email"
            )
        )
        // Record initial timeline events
        repository.insertTimelineEvent(TimelineEvent(applicationId = appId, eventType = TimelineEventType.APPLIED, eventDate = "2026-10-01"))
        repository.insertTimelineEvent(TimelineEvent(applicationId = appId, eventType = TimelineEventType.INTERVIEW_SCHEDULED, eventDate = "2026-10-12"))
        repository.insertTimelineEvent(TimelineEvent(applicationId = appId, eventType = TimelineEventType.FOLLOW_UP_SENT, eventDate = "2026-10-16"))

        val initialEventCount = repository.getTimelineEvents(appId).first().size
        assertEquals(3, initialEventCount)

        // Edit UNRELATED fields (salary, location, company name)
        val savedStateHandle = SavedStateHandle(mapOf(Screen.AddApplication.ARG_APPLICATION_ID to appId))
        val viewModel = AddEditApplicationViewModel(repository, savedStateHandle)
        testScheduler.advanceUntilIdle()

        viewModel.onCompanyNameChanged("Updated Corp")
        viewModel.onSalaryChanged("100k THB")
        viewModel.onLocationChanged("Bangkok")
        viewModel.saveApplication()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        val eventsAfter = repository.getTimelineEvents(appId).first()
        // Event count must NOT increase
        assertEquals(3, eventsAfter.size)
    }

    @Test
    fun editApplication_changingInterviewAndFollowUpDates_recordsDeterministicEvents() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Initial Corp",
                position = "Dev",
                appliedDate = "2026-10-01",
                interviewDate = "2026-10-12",
                followUpDate = "2026-10-16"
            )
        )
        repository.insertTimelineEvent(TimelineEvent(applicationId = appId, eventType = TimelineEventType.APPLIED, eventDate = "2026-10-01"))

        val savedStateHandle = SavedStateHandle(mapOf(Screen.AddApplication.ARG_APPLICATION_ID to appId))
        val viewModel = AddEditApplicationViewModel(repository, savedStateHandle)
        testScheduler.advanceUntilIdle()

        // Update interview date and follow-up date
        viewModel.onInterviewDateChanged("2026-10-20")
        viewModel.onFollowUpDateChanged("2026-10-25")
        viewModel.saveApplication()
        testScheduler.advanceUntilIdle()

        val events = repository.getTimelineEvents(appId).first()
        assertEquals(3, events.size)
        assertTrue(events.any { it.eventType == TimelineEventType.INTERVIEW_SCHEDULED && it.eventDate == "2026-10-20" })
        assertTrue(events.any { it.eventType == TimelineEventType.FOLLOW_UP_SENT && it.eventDate == "2026-10-25" })
    }

    @Test
    fun editApplication_clearingOptionalDates_persistsNullsWithoutNewEvents() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Initial Corp",
                position = "Dev",
                appliedDate = "2026-10-01",
                interviewDate = "2026-10-12",
                followUpDate = "2026-10-16"
            )
        )
        repository.insertTimelineEvent(TimelineEvent(applicationId = appId, eventType = TimelineEventType.APPLIED, eventDate = "2026-10-01"))

        val savedStateHandle = SavedStateHandle(mapOf(Screen.AddApplication.ARG_APPLICATION_ID to appId))
        val viewModel = AddEditApplicationViewModel(repository, savedStateHandle)
        testScheduler.advanceUntilIdle()

        // Clear interview date and follow-up date
        viewModel.onInterviewDateChanged("")
        viewModel.onFollowUpDateChanged("")
        viewModel.saveApplication()
        testScheduler.advanceUntilIdle()

        val updated = repository.getApplicationByIdOnce(appId)
        assertNotNull(updated)
        assertNull(updated?.interviewDate)
        assertNull(updated?.followUpDate)

        // No new events added
        val events = repository.getTimelineEvents(appId).first()
        assertEquals(1, events.size)
    }

    @Test
    fun editApplication_whenUserPreviouslyDeletedSystemEvent_unrelatedEditDoesNotRecreateIt() = runTest(testDispatcher) {
        val appId = repository.insertApplication(
            Application(
                companyName = "Initial Corp",
                position = "Dev",
                appliedDate = "2026-10-01",
                status = ApplicationStatus.INTERVIEW,
                interviewDate = "2026-10-12",
                interviewRound = "HR",
                followUpDate = "2026-10-16"
            )
        )
        // User had initially generated events, but then explicitly deleted the INTERVIEW_SCHEDULED and FOLLOW_UP_SENT events
        repository.insertTimelineEvent(TimelineEvent(applicationId = appId, eventType = TimelineEventType.APPLIED, eventDate = "2026-10-01"))

        // Only 1 event remains in repository because user deleted the interview and follow up events
        assertEquals(1, repository.getTimelineEventsOnce(appId).size)

        val savedStateHandle = SavedStateHandle(mapOf(Screen.AddApplication.ARG_APPLICATION_ID to appId))
        val viewModel = AddEditApplicationViewModel(repository, savedStateHandle)
        testScheduler.advanceUntilIdle()

        // Edit unrelated fields (company name, salary, location)
        viewModel.onCompanyNameChanged("Updated Corp")
        viewModel.onSalaryChanged("120,000 THB")
        viewModel.onLocationChanged("Bangkok")
        viewModel.saveApplication()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        val eventsAfter = repository.getTimelineEventsOnce(appId)
        // Ensure deleted events were NOT recreated!
        assertEquals(1, eventsAfter.size)
        assertEquals(TimelineEventType.APPLIED, eventsAfter[0].eventType)
    }
}

