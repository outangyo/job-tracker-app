package com.mocode.jobtracker.ui.addapplication

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mocode.jobtracker.domain.model.ApplicationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddApplicationScreen(
    onNavigateBack: () -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var jobUrl by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var appliedDate by remember { mutableStateOf("2026-10-04") }
    var selectedStatus by remember { mutableStateOf(ApplicationStatus.APPLIED) }
    var generalNotes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Application") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Job Details",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name *") },
                placeholder = { Text("e.g. Google, Line Man, Shopee") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = position,
                onValueChange = { position = it },
                label = { Text("Position *") },
                placeholder = { Text("e.g. Android Engineer, Backend Developer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = jobUrl,
                onValueChange = { jobUrl = it },
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
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Bangkok / Remote") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary") },
                    placeholder = { Text("e.g. 50k - 70k THB") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = appliedDate,
                onValueChange = { appliedDate = it },
                label = { Text("Applied Date *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Status",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == ApplicationStatus.APPLIED,
                    onClick = { selectedStatus = ApplicationStatus.APPLIED },
                    label = { Text("Applied") }
                )
                FilterChip(
                    selected = selectedStatus == ApplicationStatus.WISHLIST,
                    onClick = { selectedStatus = ApplicationStatus.WISHLIST },
                    label = { Text("Wishlist") }
                )
                FilterChip(
                    selected = selectedStatus == ApplicationStatus.INTERVIEW,
                    onClick = { selectedStatus = ApplicationStatus.INTERVIEW },
                    label = { Text("Interview") }
                )
            }

            OutlinedTextField(
                value = generalNotes,
                onValueChange = { generalNotes = it },
                label = { Text("General Notes") },
                placeholder = { Text("Add any notes, contact info, or thoughts...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Application")
            }
        }
    }
}
