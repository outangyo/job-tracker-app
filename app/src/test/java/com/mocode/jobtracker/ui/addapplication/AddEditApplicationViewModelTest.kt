package com.mocode.jobtracker.ui.addapplication

import androidx.lifecycle.SavedStateHandle
import com.mocode.jobtracker.data.repository.FakeJobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
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
}

