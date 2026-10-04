package com.mocode.jobtracker.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mocode.jobtracker.JobTrackerApplication
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.ui.addapplication.AddEditApplicationViewModel
import com.mocode.jobtracker.ui.applicationdetail.ApplicationDetailViewModel
import com.mocode.jobtracker.ui.applications.ApplicationsViewModel
import com.mocode.jobtracker.ui.dashboard.DashboardViewModel
import com.mocode.jobtracker.ui.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            DashboardViewModel(jobRepository())
        }
        initializer {
            ApplicationsViewModel(jobRepository())
        }
        initializer {
            AddEditApplicationViewModel(
                repository = jobRepository(),
                savedStateHandle = createSavedStateHandle()
            )
        }
        initializer {
            ApplicationDetailViewModel(
                repository = jobRepository(),
                savedStateHandle = createSavedStateHandle()
            )
        }
        initializer {
            SettingsViewModel()
        }
    }
}

fun CreationExtras.jobTrackerApplication(): JobTrackerApplication =
    (this[APPLICATION_KEY] as JobTrackerApplication)

fun CreationExtras.jobRepository(): JobRepository =
    jobTrackerApplication().repository
