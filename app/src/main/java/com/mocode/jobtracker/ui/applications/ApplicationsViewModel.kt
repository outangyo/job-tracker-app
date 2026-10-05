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
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class ApplicationsUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedStatuses: Set<ApplicationStatus> = emptySet(),
    val sortOption: ApplicationSortOption = ApplicationSortOption.NEWEST_APPLIED,
    val applications: List<Application> = emptyList(),
    val totalRawCount: Int = 0
)

class ApplicationsViewModel(
    repository: JobRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatuses = MutableStateFlow<Set<ApplicationStatus>>(emptySet())
    private val _sortOption = MutableStateFlow(ApplicationSortOption.NEWEST_APPLIED)

    val uiState: StateFlow<ApplicationsUiState> = combine(
        repository.getAllApplications(),
        _searchQuery,
        _selectedStatuses,
        _sortOption
    ) { allApps, query, statuses, sortOption ->
        val today = LocalDate.now().toString()

        // 1. Search + Multi-select Filter
        val filtered = allApps.filter { app ->
            val matchesStatus = statuses.isEmpty() || app.status in statuses
            val matchesQuery = query.isBlank() ||
                app.companyName.contains(query, ignoreCase = true) ||
                app.position.contains(query, ignoreCase = true)
            matchesStatus && matchesQuery
        }

        // 2. Sort
        val sorted = when (sortOption) {
            ApplicationSortOption.NEWEST_APPLIED -> {
                filtered.sortedWith(
                    compareByDescending<Application> { it.appliedDate }
                        .thenByDescending { it.id }
                )
            }
            ApplicationSortOption.OLDEST_APPLIED -> {
                filtered.sortedWith(
                    compareBy<Application> { it.appliedDate }
                        .thenBy { it.id }
                )
            }
            ApplicationSortOption.RECENTLY_UPDATED -> {
                filtered.sortedWith(
                    compareByDescending<Application> { it.updatedAt }
                        .thenByDescending { it.id }
                )
            }
            ApplicationSortOption.UPCOMING_INTERVIEW -> {
                filtered.sortedWith { a, b ->
                    val aDate = a.interviewDate?.trim()?.takeIf { it.isNotEmpty() }
                    val bDate = b.interviewDate?.trim()?.takeIf { it.isNotEmpty() }

                    fun getCategory(date: String?): Int = when {
                        date == null -> 3 // no interview date
                        date >= today -> 1 // upcoming interview
                        else -> 2 // past interview
                    }

                    val catA = getCategory(aDate)
                    val catB = getCategory(bDate)

                    if (catA != catB) {
                        catA.compareTo(catB)
                    } else if (catA == 1) {
                        // upcoming: closest date first (ascending)
                        aDate!!.compareTo(bDate!!)
                    } else if (catA == 2) {
                        // past: most recent first (descending)
                        bDate!!.compareTo(aDate!!)
                    } else {
                        // no interview date: newest applied first
                        b.appliedDate.compareTo(a.appliedDate)
                    }
                }
            }
        }

        ApplicationsUiState(
            isLoading = false,
            searchQuery = query,
            selectedStatuses = statuses,
            sortOption = sortOption,
            applications = sorted,
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

    fun onStatusToggled(status: ApplicationStatus) {
        _selectedStatuses.update { current ->
            if (status in current) {
                current - status
            } else {
                current + status
            }
        }
    }

    fun onAllStatusSelected() {
        _selectedStatuses.value = emptySet()
    }

    fun onSortOptionSelected(sortOption: ApplicationSortOption) {
        _sortOption.value = sortOption
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun clearFiltersAndSearch() {
        _searchQuery.value = ""
        _selectedStatuses.value = emptySet()
    }
}
