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

    @Test
    fun dashboard_returnsLatest3TimelineEventsGlobally_andPreservesOwningApplicationId() = runTest(testDispatcher) {
        val googleId = repository.insertApplication(
            Application(companyName = "Google", position = "Senior Android Engineer", appliedDate = "2026-10-01")
        )
        val msftId = repository.insertApplication(
            Application(companyName = "Microsoft", position = "Full Stack Engineer", appliedDate = "2026-10-01")
        )

        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = googleId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.APPLIED,
                eventDate = "2026-10-01"
            )
        )
        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = msftId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.HR_CONTACTED,
                eventDate = "2026-10-03"
            )
        )
        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = googleId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.INTERVIEW_SCHEDULED,
                eventDate = "2026-10-05"
            )
        )
        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = msftId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.INTERVIEW_COMPLETED,
                eventDate = "2026-10-07"
            )
        )

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val activities = viewModel.uiState.value.recentActivities
        // Exactly 3 latest events globally
        assertEquals(3, activities.size)

        // 1st: Microsoft Interview Completed (2026-10-07)
        assertEquals(msftId, activities[0].applicationId)
        assertEquals("Microsoft", activities[0].companyName)
        assertEquals("Full Stack Engineer", activities[0].position)
        assertEquals("2026-10-07", activities[0].event.eventDate)
        assertEquals(com.mocode.jobtracker.domain.model.TimelineEventType.INTERVIEW_COMPLETED, activities[0].event.eventType)

        // 2nd: Google Interview Scheduled (2026-10-05)
        assertEquals(googleId, activities[1].applicationId)
        assertEquals("Google", activities[1].companyName)
        assertEquals("Senior Android Engineer", activities[1].position)
        assertEquals("2026-10-05", activities[1].event.eventDate)

        // 3rd: Microsoft HR Contacted (2026-10-03)
        assertEquals(msftId, activities[2].applicationId)
        assertEquals("Microsoft", activities[2].companyName)
        assertEquals("2026-10-03", activities[2].event.eventDate)
    }

    @Test
    fun dashboard_eventsFromDifferentApplicationsAppearTogether() = runTest(testDispatcher) {
        val googleId = repository.insertApplication(Application(companyName = "Google", position = "Android Dev", appliedDate = "2026-10-01"))
        val msftId = repository.insertApplication(Application(companyName = "Microsoft", position = "Cloud Dev", appliedDate = "2026-10-01"))
        val amazonId = repository.insertApplication(Application(companyName = "Amazon", position = "Backend Dev", appliedDate = "2026-10-01"))

        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = googleId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.REJECTED,
                eventDate = "2026-10-05"
            )
        )
        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = msftId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.INTERVIEW_SCHEDULED,
                eventDate = "2026-10-04"
            )
        )
        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = amazonId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.OFFER_RECEIVED,
                eventDate = "2026-10-03"
            )
        )

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val activities = viewModel.uiState.value.recentActivities
        assertEquals(3, activities.size)
        assertEquals("Google", activities[0].companyName)
        assertEquals("Microsoft", activities[1].companyName)
        assertEquals("Amazon", activities[2].companyName)
    }

    @Test
    fun dashboard_eventsSortedNewestToOldest_andTieBreakDeterministic() = runTest(testDispatcher) {
        val appId = repository.insertApplication(Application(companyName = "Stripe", position = "Engineer", appliedDate = "2026-10-01"))

        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                id = 1,
                applicationId = appId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.APPLIED,
                eventDate = "2026-10-01",
                createdAt = 1000L
            )
        )
        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                id = 2,
                applicationId = appId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.HR_CONTACTED,
                eventDate = "2026-10-05",
                createdAt = 2000L
            )
        )
        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                id = 3,
                applicationId = appId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.INTERVIEW_SCHEDULED,
                eventDate = "2026-10-05",
                createdAt = 3000L
            )
        )

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val activities = viewModel.uiState.value.recentActivities
        assertEquals(3, activities.size)
        // Same date "2026-10-05", tie-break by createdAt DESC -> id 3 comes before id 2
        assertEquals(3L, activities[0].event.id)
        assertEquals(2L, activities[1].event.id)
        assertEquals(1L, activities[2].event.id)
    }

    @Test
    fun dashboard_moreThan3Events_exposesOnly3() = runTest(testDispatcher) {
        val appId = repository.insertApplication(Application(companyName = "Meta", position = "Dev", appliedDate = "2026-10-01"))

        for (i in 1..6) {
            repository.insertTimelineEvent(
                com.mocode.jobtracker.domain.model.TimelineEvent(
                    applicationId = appId,
                    eventType = com.mocode.jobtracker.domain.model.TimelineEventType.CUSTOM,
                    customTitle = "Event $i",
                    eventDate = "2026-10-0$i"
                )
            )
        }

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val activities = viewModel.uiState.value.recentActivities
        assertEquals(3, activities.size)
        assertEquals("Event 6", activities[0].event.displayTitle)
        assertEquals("Event 5", activities[1].event.displayTitle)
        assertEquals("Event 4", activities[2].event.displayTitle)
    }

    @Test
    fun dashboard_fewerThan3Events_showsAllAvailable() = runTest(testDispatcher) {
        val appId = repository.insertApplication(Application(companyName = "Netflix", position = "Dev", appliedDate = "2026-10-01"))

        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = appId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.APPLIED,
                eventDate = "2026-10-01"
            )
        )

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.recentActivities.size)

        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = appId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.HR_CONTACTED,
                eventDate = "2026-10-03"
            )
        )
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.recentActivities.size)
    }

    @Test
    fun dashboard_zeroEvents_emptyState() = runTest(testDispatcher) {
        repository.insertApplication(Application(companyName = "Figma", position = "Dev", appliedDate = "2026-10-01"))

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        org.junit.Assert.assertTrue(viewModel.uiState.value.recentActivities.isEmpty())
    }

    @Test
    fun dashboard_addingNewEvent_updatesFeedReactively() = runTest(testDispatcher) {
        val appId = repository.insertApplication(Application(companyName = "Apple", position = "iOS Dev", appliedDate = "2026-10-01"))

        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = appId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.APPLIED,
                eventDate = "2026-10-01"
            )
        )

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.recentActivities.size)

        // Add a newer event
        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = appId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.INTERVIEW_SCHEDULED,
                eventDate = "2026-10-08"
            )
        )
        testScheduler.advanceUntilIdle()

        val updatedActivities = viewModel.uiState.value.recentActivities
        assertEquals(2, updatedActivities.size)
        assertEquals("2026-10-08", updatedActivities[0].event.eventDate)
        assertEquals(com.mocode.jobtracker.domain.model.TimelineEventType.INTERVIEW_SCHEDULED, updatedActivities[0].event.eventType)
    }

    @Test
    fun dashboard_deletingEvent_removesItFromFeedReactively() = runTest(testDispatcher) {
        val appId = repository.insertApplication(Application(companyName = "Spotify", position = "Dev", appliedDate = "2026-10-01"))

        val event1 = com.mocode.jobtracker.domain.model.TimelineEvent(
            id = 1,
            applicationId = appId,
            eventType = com.mocode.jobtracker.domain.model.TimelineEventType.APPLIED,
            eventDate = "2026-10-01"
        )
        val event2 = com.mocode.jobtracker.domain.model.TimelineEvent(
            id = 2,
            applicationId = appId,
            eventType = com.mocode.jobtracker.domain.model.TimelineEventType.INTERVIEW_SCHEDULED,
            eventDate = "2026-10-05"
        )
        repository.insertTimelineEvent(event1)
        repository.insertTimelineEvent(event2)

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.recentActivities.size)
        assertEquals(2L, viewModel.uiState.value.recentActivities[0].event.id)

        // Delete top event
        repository.deleteTimelineEvent(event2)
        testScheduler.advanceUntilIdle()

        val activitiesAfterDelete = viewModel.uiState.value.recentActivities
        assertEquals(1, activitiesAfterDelete.size)
        assertEquals(1L, activitiesAfterDelete[0].event.id)
    }

    @Test
    fun dashboard_customEvent_displaysCustomTitleCorrectly() = runTest(testDispatcher) {
        val appId = repository.insertApplication(Application(companyName = "Airbnb", position = "Full Stack", appliedDate = "2026-10-01"))

        repository.insertTimelineEvent(
            com.mocode.jobtracker.domain.model.TimelineEvent(
                applicationId = appId,
                eventType = com.mocode.jobtracker.domain.model.TimelineEventType.CUSTOM,
                customTitle = "System Design Interview",
                eventDate = "2026-10-09",
                note = "Passed round 2"
            )
        )

        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val activities = viewModel.uiState.value.recentActivities
        assertEquals(1, activities.size)
        assertEquals("System Design Interview", activities[0].event.displayTitle)
        assertEquals("Passed round 2", activities[0].event.note)
    }
}
