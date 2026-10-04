package com.mocode.jobtracker.ui.dashboard

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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

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
    fun dashboardUiState_whenEmptyRepository_reportsZeroCounts() = runTest(testDispatcher) {
        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.totalApplications)
        assertEquals(0, state.appliedCount)
        assertEquals(0, state.interviewCount)
        assertEquals(0, state.offerCount)
        assertEquals(0, state.rejectedCount)
        assertEquals(0, state.acceptedCount)
        assertEquals(0, state.upcomingInterviews.size)
        assertEquals(0, state.recentApplications.size)
    }

    @Test
    fun dashboardUiState_calculatesCorrectStatisticsFromRealApplications() = runTest(testDispatcher) {
        repository.insertApplication(
            Application(companyName = "A", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.APPLIED)
        )
        repository.insertApplication(
            Application(companyName = "B", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.INTERVIEW, interviewDate = "2026-10-10")
        )
        repository.insertApplication(
            Application(companyName = "C", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.OFFER)
        )
        repository.insertApplication(
            Application(companyName = "D", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.REJECTED)
        )
        repository.insertApplication(
            Application(companyName = "E", position = "Dev", appliedDate = "2026-10-01", status = ApplicationStatus.ACCEPTED)
        )

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.totalApplications)
        assertEquals(1, state.appliedCount)
        assertEquals(1, state.interviewCount)
        assertEquals(1, state.offerCount)
        assertEquals(1, state.rejectedCount)
        assertEquals(1, state.acceptedCount)
        assertEquals(1, state.upcomingInterviews.size)
        assertEquals("B", state.upcomingInterviews[0].companyName)
        assertEquals(5, state.recentApplications.size)
    }
}
