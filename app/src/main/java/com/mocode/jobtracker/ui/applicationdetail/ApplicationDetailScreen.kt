package com.mocode.jobtracker.ui.applicationdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: ApplicationDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
                // Main Job Info Card
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
                            Text(text = "Location: ${app.location}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!app.salary.isNullOrBlank()) {
                            Text(text = "Salary: ${app.salary}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!app.jobUrl.isNullOrBlank()) {
                            Text(text = "URL: ${app.jobUrl}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "Applied Date: ${app.appliedDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Interview Section Card (if available)
                if (!app.interviewDate.isNullOrBlank() || !app.interviewNotes.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Event, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(text = "Interview Details", style = MaterialTheme.typography.titleMedium)
                            }
                            if (!app.interviewDate.isNullOrBlank()) {
                                Text("Date & Time: ${app.interviewDate} ${app.interviewTime.orEmpty()}")
                            }
                            if (!app.interviewRound.isNullOrBlank()) {
                                Text("Round: ${app.interviewRound}")
                            }
                            if (!app.interviewType.isNullOrBlank()) {
                                Text("Type: ${app.interviewType}")
                            }
                            if (!app.interviewNotes.isNullOrBlank()) {
                                Text(
                                    text = "Notes: ${app.interviewNotes}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Follow-up Section Card (if available)
                if (!app.followUpDate.isNullOrBlank() || !app.followUpNote.isNullOrBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(text = "Follow-up", style = MaterialTheme.typography.titleMedium)
                            }
                            if (!app.followUpDate.isNullOrBlank()) {
                                Text("Date: ${app.followUpDate}")
                            }
                            if (!app.followUpNote.isNullOrBlank()) {
                                Text("Note: ${app.followUpNote}")
                            }
                        }
                    }
                }

                // General Notes Card (if available)
                if (!app.generalNotes.isNullOrBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "Notes", style = MaterialTheme.typography.titleMedium)
                            Text(text = app.generalNotes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Timeline Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Timeline History",
                            style = MaterialTheme.typography.titleMedium
                        )
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
