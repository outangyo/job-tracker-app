package com.mocode.jobtracker.ui.dashboard

import androidx.lifecycle.ViewModel
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val totalApplications: Int = 3,
    val appliedCount: Int = 1,
    val interviewCount: Int = 1,
    val offerCount: Int = 0,
    val rejectedCount: Int = 1,
    val recentApplications: List<Application> = listOf(
        Application(
            id = 1L,
            companyName = "Google",
            position = "Android Engineer",
            appliedDate = "2026-10-01",
            status = ApplicationStatus.INTERVIEW,
            interviewDate = "2026-10-10"
        ),
        Application(
            id = 2L,
            companyName = "Agoda",
            position = "Software Engineer",
            appliedDate = "2026-09-28",
            status = ApplicationStatus.APPLIED
        ),
        Application(
            id = 3L,
            companyName = "Tech Start Corp",
            position = "Mobile Developer",
            appliedDate = "2026-09-20",
            status = ApplicationStatus.REJECTED
        )
    ),
    val upcomingInterviewCount: Int = 1,
    val upcomingFollowUpCount: Int = 0
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
}
