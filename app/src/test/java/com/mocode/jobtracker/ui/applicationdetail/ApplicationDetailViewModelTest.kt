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
}

