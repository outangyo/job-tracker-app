package com.mocode.jobtracker.ui.applications

import com.mocode.jobtracker.data.repository.FakeJobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationsViewModelTest {

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
    fun applicationsViewModel_searchMatchingCompanyName_returnsMatchingResults() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Google", position = "Android Engineer", appliedDate = "2026-10-01"))
        repository.insertApplication(Application(companyName = "Agoda", position = "Backend Developer", appliedDate = "2026-10-02"))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("goo")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.applications.size)
        assertEquals("Google", state.applications[0].companyName)
    }

    @Test
    fun applicationsViewModel_searchMatchingPosition_returnsMatchingResults() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Google", position = "Android Engineer", appliedDate = "2026-10-01"))
        repository.insertApplication(Application(companyName = "Agoda", position = "Backend Developer", appliedDate = "2026-10-02"))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("backend")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.applications.size)
        assertEquals("Agoda", state.applications[0].companyName)
    }

    @Test
    fun applicationsViewModel_searchNoMatch_returnsEmptyList() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Google", position = "Android Engineer", appliedDate = "2026-10-01"))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("xyz")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.applications.isEmpty())
        assertEquals(1, state.totalRawCount)
    }

    @Test
    fun applicationsViewModel_statusFiltering_filtersCorrectly() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "A", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))
        repository.insertApplication(Application(companyName = "B", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onStatusFilterSelected(ApplicationStatus.INTERVIEW)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.applications.size)
        assertEquals("B", state.applications[0].companyName)
    }

    @Test
    fun applicationsViewModel_combinedSearchAndStatusFilter_worksTogether() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Tech Corp", position = "Android Lead", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))
        repository.insertApplication(Application(companyName = "Tech Corp", position = "iOS Lead", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))
        repository.insertApplication(Application(companyName = "Other Corp", position = "Android Lead", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Android")
        viewModel.onStatusFilterSelected(ApplicationStatus.INTERVIEW)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.applications.size)
        assertEquals("Other Corp", state.applications[0].companyName)
    }

    @Test
    fun applicationsViewModel_clearFilters_restoresAllApplications() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Tech Corp", position = "Android Lead", appliedDate = "2026-10-01"))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("non-existent")
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.applications.isEmpty())

        viewModel.clearFilters()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertNull(state.selectedStatus)
        assertEquals(1, state.applications.size)
    }
}
