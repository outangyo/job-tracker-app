package com.mocode.jobtracker.ui.applicationdetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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

                // 5. Timeline History Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = "Timeline History",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if (uiState.timelineEvents.isEmpty()) {
                            Text(
                                text = "No timeline events recorded yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.timelineEvents.forEach { event ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = "• ${event.eventDate} — ${event.eventType.displayName}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (!event.note.isNullOrBlank()) {
                                        Text(
                                            text = "  ${event.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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
