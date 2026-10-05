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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

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
    fun applicationsViewModel_searchCaseInsensitive_returnsMatchingResults() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "SHOPEE", position = "Mobile Lead", appliedDate = "2026-10-01"))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("shopee")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.applications.size)
        assertEquals("SHOPEE", state.applications[0].companyName)
    }

    @Test
    fun applicationsViewModel_searchNoMatch_returnsEmptyList_keepsTotalRawCount() = runTest(testDispatcher) {
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
    fun applicationsViewModel_singleStatusToggle_filtersCorrectly() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "A", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))
        repository.insertApplication(Application(companyName = "B", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onStatusToggled(ApplicationStatus.INTERVIEW)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(setOf(ApplicationStatus.INTERVIEW), state.selectedStatuses)
        assertEquals(1, state.applications.size)
        assertEquals("B", state.applications[0].companyName)
    }

    @Test
    fun applicationsViewModel_multiStatusToggle_appliesOrLogic() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "App1", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))
        repository.insertApplication(Application(companyName = "App2", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))
        repository.insertApplication(Application(companyName = "App3", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.OFFER))
        repository.insertApplication(Application(companyName = "App4", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.REJECTED))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onStatusToggled(ApplicationStatus.APPLIED)
        viewModel.onStatusToggled(ApplicationStatus.INTERVIEW)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(setOf(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW), state.selectedStatuses)
        assertEquals(2, state.applications.size)
        assertTrue(state.applications.any { it.companyName == "App1" })
        assertTrue(state.applications.any { it.companyName == "App2" })
    }

    @Test
    fun applicationsViewModel_statusUntoggle_removesFromSelectedStatuses() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "App1", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))
        repository.insertApplication(Application(companyName = "App2", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onStatusToggled(ApplicationStatus.APPLIED)
        viewModel.onStatusToggled(ApplicationStatus.INTERVIEW)
        testScheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.applications.size)

        // Untoggle APPLIED
        viewModel.onStatusToggled(ApplicationStatus.APPLIED)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(setOf(ApplicationStatus.INTERVIEW), state.selectedStatuses)
        assertEquals(1, state.applications.size)
        assertEquals("App2", state.applications[0].companyName)
    }

    @Test
    fun applicationsViewModel_allButton_resetsSelectedStatusesToEmpty_showsAll() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "App1", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))
        repository.insertApplication(Application(companyName = "App2", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onStatusToggled(ApplicationStatus.APPLIED)
        testScheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.applications.size)

        // Tap All
        viewModel.onAllStatusSelected()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.selectedStatuses.isEmpty())
        assertEquals(2, state.applications.size)
    }

    @Test
    fun applicationsViewModel_allButton_retainsSearchQueryAndSortOption() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Tech Corp", position = "Android Lead", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))
        repository.insertApplication(Application(companyName = "Tech Corp", position = "Designer", appliedDate = "2026-10-02", status = ApplicationStatus.INTERVIEW))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Tech")
        viewModel.onSortOptionSelected(ApplicationSortOption.OLDEST_APPLIED)
        viewModel.onStatusToggled(ApplicationStatus.APPLIED)
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.applications.size)

        // Select All (resets statuses only)
        viewModel.onAllStatusSelected()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Tech", state.searchQuery)
        assertEquals(ApplicationSortOption.OLDEST_APPLIED, state.sortOption)
        assertTrue(state.selectedStatuses.isEmpty())
        assertEquals(2, state.applications.size)
    }

    @Test
    fun applicationsViewModel_combinedSearchAndMultiStatusFilter_worksTogether() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Tech Corp", position = "Android Lead", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))
        repository.insertApplication(Application(companyName = "Tech Corp", position = "iOS Lead", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))
        repository.insertApplication(Application(companyName = "Other Corp", position = "Android Lead", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW))
        repository.insertApplication(Application(companyName = "Other Corp", position = "Backend Lead", appliedDate = "2026-10-01", status = ApplicationStatus.OFFER))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Lead")
        viewModel.onStatusToggled(ApplicationStatus.APPLIED)
        viewModel.onStatusToggled(ApplicationStatus.INTERVIEW)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.applications.size)
        assertTrue(state.applications.none { it.status == ApplicationStatus.OFFER })
    }

    @Test
    fun applicationsViewModel_sortNewestApplied_ordersByAppliedDateDescending() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Early", position = "Dev", appliedDate = "2026-09-01"))
        repository.insertApplication(Application(companyName = "Middle", position = "Dev", appliedDate = "2026-10-01"))
        repository.insertApplication(Application(companyName = "Latest", position = "Dev", appliedDate = "2026-10-05"))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSortOptionSelected(ApplicationSortOption.NEWEST_APPLIED)
        testScheduler.advanceUntilIdle()

        val names = viewModel.uiState.value.applications.map { it.companyName }
        assertEquals(listOf("Latest", "Middle", "Early"), names)
    }

    @Test
    fun applicationsViewModel_sortOldestApplied_ordersByAppliedDateAscending() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Early", position = "Dev", appliedDate = "2026-09-01"))
        repository.insertApplication(Application(companyName = "Middle", position = "Dev", appliedDate = "2026-10-01"))
        repository.insertApplication(Application(companyName = "Latest", position = "Dev", appliedDate = "2026-10-05"))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSortOptionSelected(ApplicationSortOption.OLDEST_APPLIED)
        testScheduler.advanceUntilIdle()

        val names = viewModel.uiState.value.applications.map { it.companyName }
        assertEquals(listOf("Early", "Middle", "Latest"), names)
    }

    @Test
    fun applicationsViewModel_sortRecentlyUpdated_ordersByUpdatedAtDescending() = runTest(testDispatcher) {
        val app1Id = repository.insertApplication(Application(companyName = "First", position = "Dev", appliedDate = "2026-10-01", updatedAt = 1000L))
        val app2Id = repository.insertApplication(Application(companyName = "Second", position = "Dev", appliedDate = "2026-10-01", updatedAt = 2000L))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSortOptionSelected(ApplicationSortOption.RECENTLY_UPDATED)
        testScheduler.advanceUntilIdle()

        assertEquals("Second", viewModel.uiState.value.applications[0].companyName)

        // Update First application
        repository.updateApplication(Application(id = app1Id, companyName = "First", position = "Dev", appliedDate = "2026-10-01", updatedAt = 3000L))
        testScheduler.advanceUntilIdle()

        assertEquals("First", viewModel.uiState.value.applications[0].companyName)
    }

    @Test
    fun applicationsViewModel_sortUpcomingInterview_ordersUpcomingThenPastThenNone() = runTest(testDispatcher) {
        val today = LocalDate.now()
        val upcomingSoon = today.plusDays(2).toString()
        val upcomingLater = today.plusDays(10).toString()
        val pastInterview = today.minusDays(5).toString()

        repository.insertApplication(Application(companyName = "NoInterview", position = "Dev", appliedDate = "2026-10-01", interviewDate = null))
        repository.insertApplication(Application(companyName = "Past", position = "Dev", appliedDate = "2026-10-01", interviewDate = pastInterview))
        repository.insertApplication(Application(companyName = "UpcomingSoon", position = "Dev", appliedDate = "2026-10-01", interviewDate = upcomingSoon))
        repository.insertApplication(Application(companyName = "UpcomingLater", position = "Dev", appliedDate = "2026-10-01", interviewDate = upcomingLater))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSortOptionSelected(ApplicationSortOption.UPCOMING_INTERVIEW)
        testScheduler.advanceUntilIdle()

        val names = viewModel.uiState.value.applications.map { it.companyName }
        assertEquals(listOf("UpcomingSoon", "UpcomingLater", "Past", "NoInterview"), names)
    }

    @Test
    fun applicationsViewModel_clearSearch_clearsQueryOnly() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Google", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Goo")
        viewModel.onStatusToggled(ApplicationStatus.APPLIED)
        testScheduler.advanceUntilIdle()

        viewModel.clearSearch()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(setOf(ApplicationStatus.APPLIED), state.selectedStatuses)
    }

    @Test
    fun applicationsViewModel_clearFiltersAndSearch_resetsBoth() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Google", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED))

        val viewModel = ApplicationsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Goo")
        viewModel.onStatusToggled(ApplicationStatus.APPLIED)
        testScheduler.advanceUntilIdle()

        viewModel.clearFiltersAndSearch()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.selectedStatuses.isEmpty())
        assertEquals(1, state.applications.size)
    }
}
