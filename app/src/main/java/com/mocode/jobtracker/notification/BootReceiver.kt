package com.mocode.jobtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mocode.jobtracker.JobTrackerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as? JobTrackerApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationsEnabled = app.userPreferencesRepository.getNotificationsEnabledOnce()
                if (notificationsEnabled) {
                    app.reminderScheduler.rescheduleAllFutureReminders()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
