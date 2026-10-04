package com.mocode.jobtracker.ui.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ApplicationsUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedStatus: ApplicationStatus? = null,
    val applications: List<Application> = emptyList(),
    val totalRawCount: Int = 0
)

class ApplicationsViewModel(
    repository: JobRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatus = MutableStateFlow<ApplicationStatus?>(null)

    val uiState: StateFlow<ApplicationsUiState> = combine(
        repository.getAllApplications(),
        _searchQuery,
        _selectedStatus
    ) { allApps, query, status ->
        val filtered = allApps.filter { app ->
            val matchesStatus = status == null || app.status == status
            val matchesQuery = query.isBlank() ||
                app.companyName.contains(query, ignoreCase = true) ||
                app.position.contains(query, ignoreCase = true)
            matchesStatus && matchesQuery
        }
        ApplicationsUiState(
            isLoading = false,
            searchQuery = query,
            selectedStatus = status,
            applications = filtered,
            totalRawCount = allApps.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApplicationsUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterSelected(status: ApplicationStatus?) {
        _selectedStatus.value = status
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedStatus.value = null
    }
}
