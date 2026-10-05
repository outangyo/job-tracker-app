package com.mocode.jobtracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.domain.model.TimelineEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RecentActivityItem(
    val event: TimelineEvent,
    val companyName: String,
    val position: String
) {
    val applicationId: Long get() = event.applicationId
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalApplications: Int = 0,
    val appliedCount: Int = 0,
    val interviewCount: Int = 0,
    val offerCount: Int = 0,
    val rejectedCount: Int = 0,
    val acceptedCount: Int = 0,
    val recentApplications: List<Application> = emptyList(),
    val upcomingInterviews: List<Application> = emptyList(),
    val recentActivities: List<RecentActivityItem> = emptyList()
)

class DashboardViewModel(
    repository: JobRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllApplications(),
        repository.getAllTimelineEvents()
    ) { applications, timelineEvents ->
        val appMap = applications.associateBy { it.id }

        // Sort 3 latest TimelineEvents globally: Newest -> Oldest
        val sortedActivities = timelineEvents
            .sortedWith(
                compareByDescending<TimelineEvent> { it.eventDate }
                    .thenByDescending { it.createdAt }
                    .thenByDescending { it.id }
            )
            .mapNotNull { event ->
                val app = appMap[event.applicationId]
                if (app != null) {
                    RecentActivityItem(
                        event = event,
                        companyName = app.companyName,
                        position = app.position
                    )
                } else null
            }
            .take(3)

        DashboardUiState(
            isLoading = false,
            totalApplications = applications.size,
            appliedCount = applications.count { it.status == ApplicationStatus.APPLIED },
            interviewCount = applications.count { it.status == ApplicationStatus.INTERVIEW },
            offerCount = applications.count { it.status == ApplicationStatus.OFFER },
            rejectedCount = applications.count { it.status == ApplicationStatus.REJECTED },
            acceptedCount = applications.count { it.status == ApplicationStatus.ACCEPTED },
            recentApplications = applications.take(5),
            upcomingInterviews = applications.filter {
                !it.interviewDate.isNullOrBlank() && it.status == ApplicationStatus.INTERVIEW
            },
            recentActivities = sortedActivities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(isLoading = true)
    )
}
