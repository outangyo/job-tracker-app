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
}
