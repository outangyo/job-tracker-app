package com.mocode.jobtracker.ui.applications

import androidx.lifecycle.ViewModel
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ApplicationsUiState(
    val searchQuery: String = "",
    val selectedStatus: ApplicationStatus? = null,
    val applications: List<Application> = listOf(
        Application(
            id = 1L,
            companyName = "Google",
            position = "Android Engineer",
            appliedDate = "2026-10-01",
            status = ApplicationStatus.INTERVIEW,
            location = "Bangkok (Hybrid)",
            salary = "120,000 THB",
            interviewDate = "2026-10-10"
        ),
        Application(
            id = 2L,
            companyName = "Agoda",
            position = "Software Engineer",
            appliedDate = "2026-09-28",
            status = ApplicationStatus.APPLIED,
            location = "Bangkok",
            salary = "80k - 100k"
        ),
        Application(
            id = 3L,
            companyName = "Tech Start Corp",
            position = "Mobile Developer",
            appliedDate = "2026-09-20",
            status = ApplicationStatus.REJECTED,
            location = "Remote",
            salary = "Negotiable"
        )
    )
)

class ApplicationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onStatusFilterSelected(status: ApplicationStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
    }
}
