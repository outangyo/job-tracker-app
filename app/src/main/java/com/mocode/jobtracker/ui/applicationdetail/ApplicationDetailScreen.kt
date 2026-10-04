package com.mocode.jobtracker.ui.applicationdetail

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailScreen(
    applicationId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
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
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
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
                            text = "Google",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Interview") }
                        )
                    }
                    Text(
                        text = "Android Engineer (ID: $applicationId)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Location: Bangkok, Thailand (Hybrid)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Salary: 120,000 THB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Applied Date: 01 Oct 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Interview Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
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
                        Text(
                            text = "Interview Schedule",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text("Date & Time: 10 Oct 2026 at 14:00")
                    Text("Round: Technical Round 1")
                    Text("Type: Google Meet (Online)")
                    Text(
                        text = "Notes: Prepare Compose, Coroutines & System Design",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Follow-up Section Card
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
                        Text(
                            text = "Follow-up",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text("Date: 14 Oct 2026")
                    Text("Note: Follow up with recruiter if no response after interview.")
                }
            }

            // Timeline Preview Card
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
                    Text("• 01 Oct 2026 — Applied (Submitted via career site)")
                    Text("• 03 Oct 2026 — HR Contacted (Recruiter screened resume)")
                    Text("• 05 Oct 2026 — Interview Scheduled (Technical round on Oct 10)")
                }
            }

            OutlinedButton(
                onClick = onNavigateToEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Application")
            }
        }
    }
}
