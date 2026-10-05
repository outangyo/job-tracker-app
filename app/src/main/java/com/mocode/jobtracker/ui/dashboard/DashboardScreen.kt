package com.mocode.jobtracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.TimelineEventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Application")
            }
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
        } else if (uiState.totalApplications == 0) {
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
                    Text(
                        text = "No applications yet",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Start tracking your job search by adding your first application.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onNavigateToAdd) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("Add Application")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Statistics Section
                item {
                    Text(
                        text = "Summary Statistics",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Total", "${uiState.totalApplications}", Modifier.weight(1f))
                        StatCard("Applied", "${uiState.appliedCount}", Modifier.weight(1f))
                        StatCard("Interview", "${uiState.interviewCount}", Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Offer", "${uiState.offerCount}", Modifier.weight(1f))
                        StatCard("Rejected", "${uiState.rejectedCount}", Modifier.weight(1f))
                        StatCard("Accepted", "${uiState.acceptedCount}", Modifier.weight(1f))
                    }
                }

                // Upcoming Activity Section
                item {
                    Text(
                        text = "Upcoming Activities",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.upcomingInterviews.isEmpty()) {
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No upcoming interviews scheduled.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.upcomingInterviews.forEach { app ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDetail(app.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Event, contentDescription = null)
                                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                        Column {
                                            Text(
                                                text = "${app.companyName} — ${app.position}",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "Interview: ${app.interviewDate ?: ""} ${app.interviewTime ?: ""}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Activity Section
                item {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.recentActivities.isEmpty()) {
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No recent activity yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.recentActivities.forEach { activity ->
                                RecentActivityCard(
                                    activity = activity,
                                    onClick = { onNavigateToDetail(activity.applicationId) }
                                )
                            }
                        }
                    }
                }

                // Recent Applications Section
                item {
                    Text(
                        text = "Recent Applications",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(uiState.recentApplications) { app ->
                    RecentApplicationCard(
                        application = app,
                        onClick = { onNavigateToDetail(app.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun RecentApplicationCard(
    application: Application,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.companyName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = application.position,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Applied: ${application.appliedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            SuggestionChip(
                onClick = onClick,
                label = { Text(application.status.displayName) }
            )
        }
    }
}

@Composable
private fun RecentActivityCard(
    activity: RecentActivityItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getTimelineEventContainerColor(activity.event.eventType),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTimelineEventIcon(activity.event.eventType),
                    contentDescription = null,
                    tint = getTimelineEventContentColor(activity.event.eventType),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.event.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = activity.event.eventDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${activity.companyName} — ${activity.position}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!activity.event.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activity.event.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun getTimelineEventIcon(type: TimelineEventType): ImageVector {
    return when (type) {
        TimelineEventType.APPLIED -> Icons.Default.Email
        TimelineEventType.HR_CONTACTED -> Icons.Default.Phone
        TimelineEventType.INTERVIEW_SCHEDULED -> Icons.Default.Event
        TimelineEventType.INTERVIEW_COMPLETED -> Icons.Default.Check
        TimelineEventType.FOLLOW_UP_SENT -> Icons.Default.Email
        TimelineEventType.OFFER_RECEIVED -> Icons.Default.Star
        TimelineEventType.REJECTED -> Icons.Default.Close
        TimelineEventType.WITHDRAWN -> Icons.Default.Clear
        TimelineEventType.CUSTOM -> Icons.Default.Edit
    }
}

@Composable
private fun getTimelineEventContainerColor(type: TimelineEventType): Color {
    return when (type) {
        TimelineEventType.APPLIED -> MaterialTheme.colorScheme.primaryContainer
        TimelineEventType.HR_CONTACTED -> MaterialTheme.colorScheme.secondaryContainer
        TimelineEventType.INTERVIEW_SCHEDULED -> MaterialTheme.colorScheme.tertiaryContainer
        TimelineEventType.INTERVIEW_COMPLETED -> MaterialTheme.colorScheme.primaryContainer
        TimelineEventType.FOLLOW_UP_SENT -> MaterialTheme.colorScheme.secondaryContainer
        TimelineEventType.OFFER_RECEIVED -> MaterialTheme.colorScheme.primaryContainer
        TimelineEventType.REJECTED -> MaterialTheme.colorScheme.errorContainer
        TimelineEventType.WITHDRAWN -> MaterialTheme.colorScheme.surfaceVariant
        TimelineEventType.CUSTOM -> MaterialTheme.colorScheme.tertiaryContainer
    }
}

@Composable
private fun getTimelineEventContentColor(type: TimelineEventType): Color {
    return when (type) {
        TimelineEventType.APPLIED -> MaterialTheme.colorScheme.onPrimaryContainer
        TimelineEventType.HR_CONTACTED -> MaterialTheme.colorScheme.onSecondaryContainer
        TimelineEventType.INTERVIEW_SCHEDULED -> MaterialTheme.colorScheme.onTertiaryContainer
        TimelineEventType.INTERVIEW_COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
        TimelineEventType.FOLLOW_UP_SENT -> MaterialTheme.colorScheme.onSecondaryContainer
        TimelineEventType.OFFER_RECEIVED -> MaterialTheme.colorScheme.onPrimaryContainer
        TimelineEventType.REJECTED -> MaterialTheme.colorScheme.onErrorContainer
        TimelineEventType.WITHDRAWN -> MaterialTheme.colorScheme.onSurfaceVariant
        TimelineEventType.CUSTOM -> MaterialTheme.colorScheme.onTertiaryContainer
    }
}
