package com.mocode.jobtracker.ui.addapplication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.domain.model.TimelineEvent
import com.mocode.jobtracker.domain.model.TimelineEventType
import com.mocode.jobtracker.notification.ReminderScheduler
import com.mocode.jobtracker.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddEditFormState(
    val id: Long = 0L,
    val isEditMode: Boolean = false,
    val companyName: String = "",
    val companyNameError: String? = null,
    val position: String = "",
    val positionError: String? = null,
    val jobUrl: String = "",
    val location: String = "",
    val salary: String = "",
    val appliedDate: String = LocalDate.now().toString(),
    val appliedDateError: String? = null,
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val interviewDate: String = "",
    val interviewTime: String = "",
    val interviewRound: String = "",
    val interviewType: String = "",
    val interviewNotes: String = "",
    val followUpDate: String = "",
    val followUpNote: String = "",
    val generalNotes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class AddEditApplicationViewModel(
    private val repository: JobRepository,
    savedStateHandle: SavedStateHandle,
    private val reminderScheduler: ReminderScheduler? = null
) : ViewModel() {

    private val applicationId: Long = savedStateHandle.get<Long>(Screen.AddApplication.ARG_APPLICATION_ID) ?: -1L

    private val _uiState = MutableStateFlow(
        AddEditFormState(
            id = if (applicationId > 0) applicationId else 0L,
            isEditMode = applicationId > 0,
            isLoading = applicationId > 0
        )
    )
    val uiState: StateFlow<AddEditFormState> = _uiState.asStateFlow()

    init {
        if (applicationId > 0) {
            loadApplication(applicationId)
        }
    }

    private fun loadApplication(id: Long) {
        viewModelScope.launch {
            val app = repository.getApplicationByIdOnce(id)
            if (app != null) {
                _uiState.update { current ->
                    current.copy(
                        id = app.id,
                        isEditMode = true,
                        companyName = app.companyName,
                        position = app.position,
                        jobUrl = app.jobUrl.orEmpty(),
                        location = app.location.orEmpty(),
                        salary = app.salary.orEmpty(),
                        appliedDate = app.appliedDate,
                        status = app.status,
                        interviewDate = app.interviewDate.orEmpty(),
                        interviewTime = app.interviewTime.orEmpty(),
                        interviewRound = app.interviewRound.orEmpty(),
                        interviewType = app.interviewType.orEmpty(),
                        interviewNotes = app.interviewNotes.orEmpty(),
                        followUpDate = app.followUpDate.orEmpty(),
                        followUpNote = app.followUpNote.orEmpty(),
                        generalNotes = app.generalNotes.orEmpty(),
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Application not found") }
            }
        }
    }

    companion object {
        const val MAX_COMPANY_NAME_LENGTH = 100
        const val MAX_POSITION_LENGTH = 150
        const val MAX_LOCATION_LENGTH = 150
        const val MAX_SALARY_LENGTH = 100
        const val MAX_JOB_URL_LENGTH = 500
        const val MAX_INTERVIEW_ROUND_LENGTH = 50
        const val MAX_INTERVIEW_TYPE_LENGTH = 50
        const val MAX_INTERVIEW_TIME_LENGTH = 20
        const val MAX_INTERVIEW_NOTES_LENGTH = 1000
        const val MAX_FOLLOW_UP_NOTE_LENGTH = 1000
        const val MAX_GENERAL_NOTES_LENGTH = 2000
    }

    fun onCompanyNameChanged(name: String) {
        _uiState.update { it.copy(companyName = name.take(MAX_COMPANY_NAME_LENGTH), companyNameError = null) }
    }

    fun onPositionChanged(pos: String) {
        _uiState.update { it.copy(position = pos.take(MAX_POSITION_LENGTH), positionError = null) }
    }

    fun onJobUrlChanged(url: String) {
        _uiState.update { it.copy(jobUrl = url.take(MAX_JOB_URL_LENGTH)) }
    }

    fun onLocationChanged(loc: String) {
        _uiState.update { it.copy(location = loc.take(MAX_LOCATION_LENGTH)) }
    }

    fun onSalaryChanged(sal: String) {
        _uiState.update { it.copy(salary = sal.take(MAX_SALARY_LENGTH)) }
    }

    fun onAppliedDateChanged(date: String) {
        _uiState.update { it.copy(appliedDate = date, appliedDateError = null) }
    }

    fun onStatusChanged(status: ApplicationStatus) {
        _uiState.update { it.copy(status = status) }
    }

    fun onInterviewDateChanged(date: String) {
        _uiState.update { it.copy(interviewDate = date) }
    }

    fun onInterviewTimeChanged(time: String) {
        _uiState.update { it.copy(interviewTime = time.take(MAX_INTERVIEW_TIME_LENGTH)) }
    }

    fun onInterviewRoundChanged(round: String) {
        _uiState.update { it.copy(interviewRound = round.take(MAX_INTERVIEW_ROUND_LENGTH)) }
    }

    fun onInterviewTypeChanged(type: String) {
        _uiState.update { it.copy(interviewType = type.take(MAX_INTERVIEW_TYPE_LENGTH)) }
    }

    fun onInterviewNotesChanged(notes: String) {
        _uiState.update { it.copy(interviewNotes = notes.take(MAX_INTERVIEW_NOTES_LENGTH)) }
    }

    fun onFollowUpDateChanged(date: String) {
        _uiState.update { it.copy(followUpDate = date) }
    }

    fun onFollowUpNoteChanged(note: String) {
        _uiState.update { it.copy(followUpNote = note.take(MAX_FOLLOW_UP_NOTE_LENGTH)) }
    }

    fun onGeneralNotesChanged(notes: String) {
        _uiState.update { it.copy(generalNotes = notes.take(MAX_GENERAL_NOTES_LENGTH)) }
    }

    fun saveApplication() {
        val state = _uiState.value
        var hasError = false

        val companyError = if (state.companyName.isBlank()) {
            hasError = true
            "Company name is required."
        } else null

        val positionError = if (state.position.isBlank()) {
            hasError = true
            "Position is required."
        } else null

        val dateError = if (state.appliedDate.isBlank()) {
            hasError = true
            "Applied date is required."
        } else null

        if (hasError) {
            _uiState.update {
                it.copy(
                    companyNameError = companyError,
                    positionError = positionError,
                    appliedDateError = dateError
                )
            }
            return
        }

        viewModelScope.launch {
            if (state.isEditMode) {
                val existing = repository.getApplicationByIdOnce(state.id)
                val updatedApp = Application(
                    id = state.id,
                    companyName = state.companyName.trim(),
                    position = state.position.trim(),
                    jobUrl = state.jobUrl.trim().ifEmpty { null },
                    location = state.location.trim().ifEmpty { null },
                    salary = state.salary.trim().ifEmpty { null },
                    appliedDate = state.appliedDate.trim(),
                    status = state.status,
                    interviewDate = state.interviewDate.trim().ifEmpty { null },
                    interviewTime = state.interviewTime.trim().ifEmpty { null },
                    interviewRound = state.interviewRound.trim().ifEmpty { null },
                    interviewType = state.interviewType.trim().ifEmpty { null },
                    interviewNotes = state.interviewNotes.trim().ifEmpty { null },
                    followUpDate = state.followUpDate.trim().ifEmpty { null },
                    followUpNote = state.followUpNote.trim().ifEmpty { null },
                    generalNotes = state.generalNotes.trim().ifEmpty { null },
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateApplication(updatedApp)

                // 1. If status changed, create a timeline event to maintain history
                var statusInterviewEventInserted = false
                if (existing != null && existing.status != state.status) {
                    val eventType = when (state.status) {
                        ApplicationStatus.APPLIED -> TimelineEventType.APPLIED
                        ApplicationStatus.INTERVIEW -> TimelineEventType.INTERVIEW_SCHEDULED
                        ApplicationStatus.OFFER -> TimelineEventType.OFFER_RECEIVED
                        ApplicationStatus.REJECTED -> TimelineEventType.REJECTED
                        ApplicationStatus.WITHDRAWN -> TimelineEventType.WITHDRAWN
                        else -> TimelineEventType.CUSTOM
                    }
                    if (state.status == ApplicationStatus.INTERVIEW && state.interviewDate.isNotBlank()) {
                        statusInterviewEventInserted = true
                        repository.insertTimelineEvent(
                            TimelineEvent(
                                applicationId = state.id,
                                eventType = TimelineEventType.INTERVIEW_SCHEDULED,
                                eventDate = state.interviewDate.trim(),
                                note = "Interview scheduled (${state.interviewRound.ifBlank { "Round 1" }})"
                            )
                        )
                    } else {
                        repository.insertTimelineEvent(
                            TimelineEvent(
                                applicationId = state.id,
                                eventType = eventType,
                                eventDate = LocalDate.now().toString(),
                                note = "Status changed to ${state.status.displayName}"
                            )
                        )
                    }
                }

                // 2. If interview date was added or changed (and not already inserted above for status change)
                val existingInterviewDate = existing?.interviewDate?.trim().orEmpty()
                val newInterviewDate = state.interviewDate.trim()
                if (!statusInterviewEventInserted && newInterviewDate.isNotBlank() && newInterviewDate != existingInterviewDate) {
                    repository.insertTimelineEvent(
                        TimelineEvent(
                            applicationId = state.id,
                            eventType = TimelineEventType.INTERVIEW_SCHEDULED,
                            eventDate = newInterviewDate,
                            note = "Interview scheduled (${state.interviewRound.ifBlank { "Round 1" }})"
                        )
                    )
                }

                // 3. If follow-up date was added or changed
                val existingFollowUpDate = existing?.followUpDate?.trim().orEmpty()
                val newFollowUpDate = state.followUpDate.trim()
                if (newFollowUpDate.isNotBlank() && newFollowUpDate != existingFollowUpDate) {
                    repository.insertTimelineEvent(
                        TimelineEvent(
                            applicationId = state.id,
                            eventType = TimelineEventType.FOLLOW_UP_SENT,
                            eventDate = newFollowUpDate,
                            note = if (state.followUpNote.isNotBlank()) "Follow-up: ${state.followUpNote.trim()}" else "Follow-up scheduled"
                        )
                    )
                }

                // 4. Update reminders for Edit Mode
                if (updatedApp.interviewDate.isNullOrBlank()) {
                    reminderScheduler?.cancelInterviewReminder(updatedApp.id)
                } else if (updatedApp.interviewDate != existing?.interviewDate || updatedApp.interviewTime != existing?.interviewTime) {
                    reminderScheduler?.scheduleInterviewReminder(updatedApp)
                }

                if (updatedApp.followUpDate.isNullOrBlank()) {
                    reminderScheduler?.cancelFollowUpReminder(updatedApp.id)
                } else if (updatedApp.followUpDate != existing?.followUpDate || updatedApp.followUpNote != existing?.followUpNote) {
                    reminderScheduler?.scheduleFollowUpReminder(updatedApp)
                }
            } else {
                val newApp = Application(
                    companyName = state.companyName.trim(),
                    position = state.position.trim(),
                    jobUrl = state.jobUrl.trim().ifEmpty { null },
                    location = state.location.trim().ifEmpty { null },
                    salary = state.salary.trim().ifEmpty { null },
                    appliedDate = state.appliedDate.trim(),
                    status = state.status,
                    interviewDate = state.interviewDate.trim().ifEmpty { null },
                    interviewTime = state.interviewTime.trim().ifEmpty { null },
                    interviewRound = state.interviewRound.trim().ifEmpty { null },
                    interviewType = state.interviewType.trim().ifEmpty { null },
                    interviewNotes = state.interviewNotes.trim().ifEmpty { null },
                    followUpDate = state.followUpDate.trim().ifEmpty { null },
                    followUpNote = state.followUpNote.trim().ifEmpty { null },
                    generalNotes = state.generalNotes.trim().ifEmpty { null },
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                val newId = repository.insertApplication(newApp)

                // Add initial timeline event
                repository.insertTimelineEvent(
                    TimelineEvent(
                        applicationId = newId,
                        eventType = TimelineEventType.APPLIED,
                        eventDate = state.appliedDate.trim(),
                        note = "Application submitted"
                    )
                )

                if (state.interviewDate.isNotBlank()) {
                    repository.insertTimelineEvent(
                        TimelineEvent(
                            applicationId = newId,
                            eventType = TimelineEventType.INTERVIEW_SCHEDULED,
                            eventDate = state.interviewDate.trim(),
                            note = "Interview scheduled (${state.interviewRound.ifBlank { "Round 1" }})"
                        )
                    )
                }

                if (state.followUpDate.isNotBlank()) {
                    repository.insertTimelineEvent(
                        TimelineEvent(
                            applicationId = newId,
                            eventType = TimelineEventType.FOLLOW_UP_SENT,
                            eventDate = state.followUpDate.trim(),
                            note = if (state.followUpNote.isNotBlank()) "Follow-up: ${state.followUpNote.trim()}" else "Follow-up scheduled"
                        )
                    )
                }

                // Schedule reminders for new application
                val savedApp = newApp.copy(id = newId)
                if (!savedApp.interviewDate.isNullOrBlank()) {
                    reminderScheduler?.scheduleInterviewReminder(savedApp)
                }
                if (!savedApp.followUpDate.isNullOrBlank()) {
                    reminderScheduler?.scheduleFollowUpReminder(savedApp)
                }
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
