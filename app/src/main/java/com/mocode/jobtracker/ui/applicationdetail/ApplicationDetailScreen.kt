package com.mocode.jobtracker.ui.applicationdetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mocode.jobtracker.domain.model.TimelineEvent
import com.mocode.jobtracker.domain.model.TimelineEventType
import com.mocode.jobtracker.ui.common.AppDatePickerDialog
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: ApplicationDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    // 1. Delete Application Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            title = { Text("Delete application?") },
            text = { Text("This will permanently remove this application and its timeline history.") },
            confirmButton = {
                Button(
                    onClick = viewModel::onConfirmDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Delete Timeline Event Confirmation Dialog
    if (uiState.eventToDelete != null) {
        val event = uiState.eventToDelete!!
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteEventDialog,
            title = { Text("Delete timeline event?") },
            text = { Text("Are you sure you want to delete '${event.displayTitle}' (${event.eventDate})?") },
            confirmButton = {
                Button(
                    onClick = viewModel::onConfirmDeleteEvent,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteEventDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Add / Edit Timeline Event Dialog
    if (uiState.isAddingEvent || uiState.eventBeingEdited != null) {
        TimelineEventDialog(
            initialEvent = uiState.eventBeingEdited,
            onSave = { eventType, customTitle, eventDate, note ->
                viewModel.saveTimelineEvent(eventType, customTitle, eventDate, note)
            },
            onDismiss = viewModel::onDismissEventDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    uiState.application?.let { app ->
                        IconButton(onClick = { onNavigateToEdit(app.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = viewModel::onDeleteClicked) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
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
        } else if (uiState.application == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Application not found", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            }
        } else {
            val app = uiState.application!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Main Job Information Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.companyName,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(app.status.displayName) }
                            )
                        }
                        Text(
                            text = app.position,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!app.location.isNullOrBlank()) {
                            Text(text = "📍 ${app.location}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!app.salary.isNullOrBlank()) {
                            Text(text = "💰 ${app.salary}", style = MaterialTheme.typography.bodyMedium)
                        }

                        // Job URL UX: visually identifiable link + external browser open action
                        if (!app.jobUrl.isNullOrBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { openJobUrl(context, app.jobUrl) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = app.jobUrl,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { openJobUrl(context, app.jobUrl) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Open Job Posting URL",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Applied Date: ${app.appliedDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // 2. Interview Details Card (with clean empty state)
                val hasInterview = !app.interviewDate.isNullOrBlank() ||
                    !app.interviewTime.isNullOrBlank() ||
                    !app.interviewRound.isNullOrBlank() ||
                    !app.interviewType.isNullOrBlank() ||
                    !app.interviewNotes.isNullOrBlank()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (hasInterview) {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    } else {
                        CardDefaults.cardColors()
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Event, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(text = "Interview Details", style = MaterialTheme.typography.titleMedium)
                        }

                        if (hasInterview) {
                            if (!app.interviewDate.isNullOrBlank()) {
                                val dateTime = buildString {
                                    append(app.interviewDate)
                                    if (!app.interviewTime.isNullOrBlank()) {
                                        append(" at ")
                                        append(app.interviewTime)
                                    }
                                }
                                Text("Date & Time: $dateTime", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!app.interviewRound.isNullOrBlank() || !app.interviewType.isNullOrBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!app.interviewRound.isNullOrBlank()) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("Round: ${app.interviewRound}") }
                                        )
                                    }
                                    if (!app.interviewType.isNullOrBlank()) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("Type: ${app.interviewType}") }
                                        )
                                    }
                                }
                            }
                            if (!app.interviewNotes.isNullOrBlank()) {
                                Text(
                                    text = "Notes: ${app.interviewNotes}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Text(
                                text = "No interview scheduled yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 3. Follow-up Reminder Card (with clean empty state)
                val hasFollowUp = !app.followUpDate.isNullOrBlank() || !app.followUpNote.isNullOrBlank()

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(text = "Follow-up Reminder", style = MaterialTheme.typography.titleMedium)
                        }

                        if (hasFollowUp) {
                            if (!app.followUpDate.isNullOrBlank()) {
                                Text("Date: ${app.followUpDate}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!app.followUpNote.isNullOrBlank()) {
                                Text("Note: ${app.followUpNote}", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Text(
                                text = "No follow-up reminder set.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 4. General Notes Card (with clean empty state)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(text = "Notes", style = MaterialTheme.typography.titleMedium)
                        }
                        if (!app.generalNotes.isNullOrBlank()) {
                            Text(text = app.generalNotes, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(
                                text = "No notes added.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 5. Timeline History Card (Chronological Newest -> Oldest with Add, Edit, Delete)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.History, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "Timeline History",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            OutlinedButton(
                                onClick = viewModel::onAddEventClicked,
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                Text("Add Event")
                            }
                        }

                        if (uiState.timelineEvents.isEmpty()) {
                            Text(
                                text = "No timeline events recorded yet. Tap '+ Add Event' to log your job progress.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.timelineEvents.forEachIndexed { index, event ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = event.displayTitle,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = event.eventDate,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        if (!event.note.isNullOrBlank()) {
                                            Text(
                                                text = event.note,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.onEditEventClicked(event) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit event",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.onDeleteEventClicked(event) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete event",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                if (index < uiState.timelineEvents.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { onNavigateToEdit(app.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Application")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineEventDialog(
    initialEvent: TimelineEvent?,
    onSave: (eventType: TimelineEventType, customTitle: String?, eventDate: String, note: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialEvent?.eventType ?: TimelineEventType.APPLIED) }
    var customTitle by remember { mutableStateOf(initialEvent?.customTitle.orEmpty()) }
    var customTitleError by remember { mutableStateOf<String?>(null) }
    var eventDate by remember { mutableStateOf(initialEvent?.eventDate ?: LocalDate.now().toString()) }
    var note by remember { mutableStateOf(initialEvent?.note.orEmpty()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDate = eventDate,
            onDateSelected = {
                eventDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialEvent != null) "Edit Timeline Event" else "Add Timeline Event")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Event Type *", style = MaterialTheme.typography.bodyMedium)

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimelineEventType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                if (type != TimelineEventType.CUSTOM) {
                                    customTitleError = null
                                }
                            },
                            label = { Text(type.displayName) }
                        )
                    }
                }

                if (selectedType == TimelineEventType.CUSTOM) {
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = {
                            customTitle = it
                            customTitleError = null
                        },
                        label = { Text("Custom Event Title *") },
                        placeholder = { Text("e.g. Coding Test, Portfolio Review") },
                        isError = customTitleError != null,
                        supportingText = customTitleError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Event Date *") },
                    placeholder = { Text("YYYY-MM-DD") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select event date"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("Details or outcomes...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedType == TimelineEventType.CUSTOM && customTitle.isBlank()) {
                        customTitleError = "Custom title is required"
                        return@Button
                    }
                    onSave(selectedType, customTitle, eventDate, note)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun openJobUrl(context: Context, url: String) {
    try {
        val trimmed = url.trim()
        val webUrl = if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        context.startActivity(intent)
    } catch (_: Exception) {
        // Safe fallback if device has no activity to handle browser intent
    }
}
