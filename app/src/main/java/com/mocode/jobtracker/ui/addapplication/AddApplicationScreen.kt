package com.mocode.jobtracker.ui.addapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.ui.common.AppDatePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private enum class DateField {
    APPLIED,
    INTERVIEW,
    FOLLOW_UP
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddApplicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditApplicationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeDateField by remember { mutableStateOf<DateField?>(null) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    activeDateField?.let { field ->
        val currentDate = when (field) {
            DateField.APPLIED -> uiState.appliedDate
            DateField.INTERVIEW -> uiState.interviewDate
            DateField.FOLLOW_UP -> uiState.followUpDate
        }
        AppDatePickerDialog(
            initialDate = currentDate,
            onDateSelected = { selectedDate ->
                when (field) {
                    DateField.APPLIED -> viewModel.onAppliedDateChanged(selectedDate)
                    DateField.INTERVIEW -> viewModel.onInterviewDateChanged(selectedDate)
                    DateField.FOLLOW_UP -> viewModel.onFollowUpDateChanged(selectedDate)
                }
                activeDateField = null
            },
            onDismiss = { activeDateField = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Edit Application" else "New Application")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Job Information",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = viewModel::onCompanyNameChanged,
                    label = { Text("Company Name *") },
                    placeholder = { Text("e.g. Google, Line, Shopee") },
                    isError = uiState.companyNameError != null,
                    supportingText = uiState.companyNameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.position,
                    onValueChange = viewModel::onPositionChanged,
                    label = { Text("Position *") },
                    placeholder = { Text("e.g. Android Engineer") },
                    isError = uiState.positionError != null,
                    supportingText = uiState.positionError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.jobUrl,
                    onValueChange = viewModel::onJobUrlChanged,
                    label = { Text("Job URL (Optional)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.location,
                        onValueChange = viewModel::onLocationChanged,
                        label = { Text("Location") },
                        placeholder = { Text("e.g. Bangkok / Remote") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.salary,
                        onValueChange = viewModel::onSalaryChanged,
                        label = { Text("Salary") },
                        placeholder = { Text("e.g. 50k - 70k THB") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = uiState.appliedDate,
                    onValueChange = viewModel::onAppliedDateChanged,
                    label = { Text("Applied Date *") },
                    placeholder = { Text("YYYY-MM-DD") },
                    isError = uiState.appliedDateError != null,
                    supportingText = uiState.appliedDateError?.let { { Text(it) } },
                    trailingIcon = {
                        IconButton(onClick = { activeDateField = DateField.APPLIED }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select applied date"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Application Status *",
                    style = MaterialTheme.typography.bodyLarge
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ApplicationStatus.entries.forEach { status ->
                        FilterChip(
                            selected = uiState.status == status,
                            onClick = { viewModel.onStatusChanged(status) },
                            label = { Text(status.displayName) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Interview Section
                Text(
                    text = "Interview Information (Optional)",
                    style = MaterialTheme.typography.titleMedium
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.interviewDate,
                                onValueChange = viewModel::onInterviewDateChanged,
                                label = { Text("Interview Date") },
                                placeholder = { Text("YYYY-MM-DD") },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (uiState.interviewDate.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.onInterviewDateChanged("") }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear interview date"
                                                )
                                            }
                                        }
                                        IconButton(onClick = { activeDateField = DateField.INTERVIEW }) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Select interview date"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.interviewTime,
                                onValueChange = viewModel::onInterviewTimeChanged,
                                label = { Text("Time (Optional)") },
                                placeholder = { Text("14:00") },
                                trailingIcon = if (uiState.interviewTime.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { viewModel.onInterviewTimeChanged("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear time"
                                            )
                                        }
                                    }
                                } else null,
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = uiState.interviewRound,
                                onValueChange = viewModel::onInterviewRoundChanged,
                                label = { Text("Round (Optional)") },
                                placeholder = { Text("e.g. HR / Technical / Final") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("HR", "Technical", "Final", "Other").forEach { round ->
                                    SuggestionChip(
                                        onClick = { viewModel.onInterviewRoundChanged(round) },
                                        label = { Text(round, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = uiState.interviewType,
                                onValueChange = viewModel::onInterviewTypeChanged,
                                label = { Text("Type (Optional)") },
                                placeholder = { Text("e.g. Online / On-site / Phone") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Online", "On-site", "Phone", "Other").forEach { type ->
                                    SuggestionChip(
                                        onClick = { viewModel.onInterviewTypeChanged(type) },
                                        label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.interviewNotes,
                            onValueChange = viewModel::onInterviewNotesChanged,
                            label = { Text("Interview Notes") },
                            placeholder = { Text("Notes, interviewer names, topics to prepare...") },
                            supportingText = {
                                Text(
                                    text = "${uiState.interviewNotes.length}/${AddEditApplicationViewModel.MAX_INTERVIEW_NOTES_LENGTH}",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Follow-up Section
                Text(
                    text = "Follow-up Reminder (Optional)",
                    style = MaterialTheme.typography.titleMedium
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.followUpDate,
                            onValueChange = viewModel::onFollowUpDateChanged,
                            label = { Text("Follow-up Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (uiState.followUpDate.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onFollowUpDateChanged("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear follow-up date"
                                            )
                                        }
                                    }
                                    IconButton(onClick = { activeDateField = DateField.FOLLOW_UP }) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Select follow-up date"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.followUpNote,
                            onValueChange = viewModel::onFollowUpNoteChanged,
                            label = { Text("Follow-up Note") },
                            placeholder = { Text("e.g. Email HR if no update by Friday") },
                            supportingText = {
                                Text(
                                    text = "${uiState.followUpNote.length}/${AddEditApplicationViewModel.MAX_FOLLOW_UP_NOTE_LENGTH}",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // General Notes Section
                Text(
                    text = "General Notes",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = uiState.generalNotes,
                    onValueChange = viewModel::onGeneralNotesChanged,
                    label = { Text("Notes") },
                    placeholder = { Text("Any general thoughts, contact person, or referral info...") },
                    supportingText = {
                        Text(
                            text = "${uiState.generalNotes.length}/${AddEditApplicationViewModel.MAX_GENERAL_NOTES_LENGTH}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = viewModel::saveApplication,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isEditMode) "Save Changes" else "Save Application")
                }
            }
        }
    }
}


