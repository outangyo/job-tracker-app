package com.mocode.jobtracker

import android.app.Application
import com.mocode.jobtracker.data.local.JobTrackerDatabase
import com.mocode.jobtracker.data.preferences.DataStoreUserPreferencesRepository
import com.mocode.jobtracker.data.preferences.UserPreferencesRepository
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.data.repository.RoomJobRepository
import com.mocode.jobtracker.notification.AndroidReminderScheduler
import com.mocode.jobtracker.notification.NotificationHelper
import com.mocode.jobtracker.notification.ReminderScheduler

class JobTrackerApplication : Application() {

    val database: JobTrackerDatabase by lazy {
        JobTrackerDatabase.getDatabase(this)
    }

    val repository: JobRepository by lazy {
        RoomJobRepository(
            applicationDao = database.applicationDao(),
            timelineEventDao = database.timelineEventDao()
        )
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        DataStoreUserPreferencesRepository(this)
    }

    val reminderScheduler: ReminderScheduler by lazy {
        AndroidReminderScheduler(
            context = this,
            repository = repository,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(this)
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
    }
}
