package com.mocode.jobtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mocode.jobtracker.JobTrackerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMINDER = "com.mocode.jobtracker.ACTION_REMINDER"
        const val EXTRA_APPLICATION_ID = "extra_application_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return

        val applicationId = intent.getLongExtra(EXTRA_APPLICATION_ID, -1L)
        if (applicationId <= 0) return

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Job Application Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have an upcoming event."

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? JobTrackerApplication
                val notificationsEnabled = app?.userPreferencesRepository?.getNotificationsEnabledOnce() ?: true
                if (notificationsEnabled) {
                    val notificationHelper = NotificationHelper(context)
                    notificationHelper.showNotification(
                        applicationId = applicationId,
                        notificationId = notificationId,
                        title = title,
                        message = message
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
