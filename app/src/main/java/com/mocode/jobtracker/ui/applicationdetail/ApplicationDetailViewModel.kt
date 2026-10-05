package com.mocode.jobtracker.ui.applicationdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.TimelineEvent
import com.mocode.jobtracker.domain.model.TimelineEventType
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
    val showDeleteDialog: Boolean = false,
    val isAddingEvent: Boolean = false,
    val eventBeingEdited: TimelineEvent? = null,
    val eventToDelete: TimelineEvent? = null
)

class ApplicationDetailViewModel(
    private val repository: JobRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val applicationId: Long = savedStateHandle.get<Long>(Screen.ApplicationDetail.ARG_APPLICATION_ID) ?: -1L

    private val _deleteAppDialogState = MutableStateFlow(false)
    private val _isDeleted = MutableStateFlow(false)
    private val _isAddingEvent = MutableStateFlow(false)
    private val _eventBeingEdited = MutableStateFlow<TimelineEvent?>(null)
    private val _eventToDelete = MutableStateFlow<TimelineEvent?>(null)

    val uiState: StateFlow<ApplicationDetailUiState> = combine(
        repository.getApplicationById(applicationId),
        repository.getTimelineEvents(applicationId),
        _deleteAppDialogState,
        _isDeleted,
        _isAddingEvent,
        _eventBeingEdited,
        _eventToDelete
    ) { args: Array<Any?> ->
        val app = args[0] as Application?
        @Suppress("UNCHECKED_CAST")
        val events = (args[1] as? List<TimelineEvent>) ?: emptyList()
        val showDeleteApp = args[2] as Boolean
        val isDeleted = args[3] as Boolean
        val isAddingEvent = args[4] as Boolean
        val eventBeingEdited = args[5] as TimelineEvent?
        val eventToDelete = args[6] as TimelineEvent?

        // Sort events chronologically: Newest -> Oldest
        val sortedEvents = events.sortedWith(
            compareByDescending<TimelineEvent> { it.eventDate }
                .thenByDescending { it.createdAt }
                .thenByDescending { it.id }
        )

        ApplicationDetailUiState(
            application = app,
            timelineEvents = sortedEvents,
            isLoading = false,
            isDeleted = isDeleted,
            showDeleteDialog = showDeleteApp,
            isAddingEvent = isAddingEvent,
            eventBeingEdited = eventBeingEdited,
            eventToDelete = eventToDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApplicationDetailUiState(isLoading = true)
    )

    fun onDeleteClicked() {
        _deleteAppDialogState.update { true }
    }

    fun onDismissDeleteDialog() {
        _deleteAppDialogState.update { false }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            _deleteAppDialogState.update { false }
            repository.deleteApplicationById(applicationId)
            _isDeleted.update { true }
        }
    }

    // Timeline Event Actions
    fun onAddEventClicked() {
        _eventBeingEdited.value = null
        _isAddingEvent.value = true
    }

    fun onEditEventClicked(event: TimelineEvent) {
        _isAddingEvent.value = false
        _eventBeingEdited.value = event
    }

    fun onDismissEventDialog() {
        _isAddingEvent.value = false
        _eventBeingEdited.value = null
    }

    fun saveTimelineEvent(
        eventType: TimelineEventType,
        customTitle: String?,
        eventDate: String,
        note: String?
    ) {
        viewModelScope.launch {
            val editing = _eventBeingEdited.value
            val cleanedCustomTitle = if (eventType == TimelineEventType.CUSTOM) {
                customTitle?.trim()?.ifEmpty { null }
            } else null
            val cleanedNote = note?.trim()?.ifEmpty { null }
            val cleanedDate = eventDate.trim()

            if (editing != null) {
                val updatedEvent = editing.copy(
                    eventType = eventType,
                    customTitle = cleanedCustomTitle,
                    eventDate = cleanedDate,
                    note = cleanedNote
                )
                repository.updateTimelineEvent(updatedEvent)
            } else {
                val newEvent = TimelineEvent(
                    applicationId = applicationId,
                    eventType = eventType,
                    customTitle = cleanedCustomTitle,
                    eventDate = cleanedDate,
                    note = cleanedNote
                )
                repository.insertTimelineEvent(newEvent)
            }
            _isAddingEvent.value = false
            _eventBeingEdited.value = null
        }
    }

    fun onDeleteEventClicked(event: TimelineEvent) {
        _eventToDelete.value = event
    }

    fun onDismissDeleteEventDialog() {
        _eventToDelete.value = null
    }

    fun onConfirmDeleteEvent() {
        viewModelScope.launch {
            val event = _eventToDelete.value
            if (event != null) {
                repository.deleteTimelineEvent(event)
            }
            _eventToDelete.value = null
        }
    }
}
