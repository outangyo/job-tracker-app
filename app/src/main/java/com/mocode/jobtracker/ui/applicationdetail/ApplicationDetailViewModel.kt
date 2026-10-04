package com.mocode.jobtracker.ui.applicationdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.TimelineEvent
import com.mocode.jobtracker.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApplicationDetailUiState(
    val application: Application? = null,
    val timelineEvents: List<TimelineEvent> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false
)

class ApplicationDetailViewModel(
    private val repository: JobRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val applicationId: Long = savedStateHandle.get<Long>(Screen.ApplicationDetail.ARG_APPLICATION_ID) ?: -1L

    private val _dialogState = MutableStateFlow(false)
    private val _isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<ApplicationDetailUiState> = combine(
        repository.getApplicationById(applicationId),
        repository.getTimelineEvents(applicationId),
        _dialogState,
        _isDeleted
    ) { app, events, showDialog, isDeleted ->
        ApplicationDetailUiState(
            application = app,
            timelineEvents = events,
            isLoading = false,
            isDeleted = isDeleted,
            showDeleteDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApplicationDetailUiState(isLoading = true)
    )

    fun onDeleteClicked() {
        _dialogState.update { true }
    }

    fun onDismissDeleteDialog() {
        _dialogState.update { false }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            _dialogState.update { false }
            repository.deleteApplicationById(applicationId)
            _isDeleted.update { true }
        }
    }
}
