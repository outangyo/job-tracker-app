package com.mocode.jobtracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalApplications: Int = 0,
    val appliedCount: Int = 0,
    val interviewCount: Int = 0,
    val offerCount: Int = 0,
    val rejectedCount: Int = 0,
    val acceptedCount: Int = 0,
    val recentApplications: List<Application> = emptyList(),
    val upcomingInterviews: List<Application> = emptyList()
)

class DashboardViewModel(
    repository: JobRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.getAllApplications()
        .map { applications ->
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
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(isLoading = true)
        )
}
