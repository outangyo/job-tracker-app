package com.mocode.jobtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mocode.jobtracker.data.preferences.UserPreferencesRepository
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.domain.model.Application
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AndroidReminderScheduler(
    private val context: Context,
    private val repository: JobRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationHelper = NotificationHelper(context)

    override fun scheduleInterviewReminder(application: Application) {
        val triggerMillis = calculateInterviewTriggerMillis(
            dateStr = application.interviewDate,
            timeStr = application.interviewTime
        )

        val notificationId = NotificationHelper.getInterviewNotificationId(application.id)
        if (triggerMillis == null || triggerMillis <= System.currentTimeMillis()) {
            // Either missing date or already in the past
            cancelInterviewReminder(application.id)
            return
        }

        val title = "Interview: ${application.companyName}"
        val message = buildString {
            append("${application.position}")
            if (!application.interviewRound.isNullOrBlank()) {
                append(" (${application.interviewRound})")
            }
            if (!application.interviewTime.isNullOrBlank()) {
                append(" at ${application.interviewTime}")
            }
        }

        scheduleAlarm(
            requestCode = notificationId,
            applicationId = application.id,
            triggerMillis = triggerMillis,
            title = title,
            message = message
        )
    }

    override fun scheduleFollowUpReminder(application: Application) {
        val triggerMillis = calculateFollowUpTriggerMillis(application.followUpDate)
        val notificationId = NotificationHelper.getFollowUpNotificationId(application.id)

        if (triggerMillis == null || triggerMillis <= System.currentTimeMillis()) {
            cancelFollowUpReminder(application.id)
            return
        }

        val title = "Follow-up: ${application.companyName}"
        val message = if (!application.followUpNote.isNullOrBlank()) {
            application.followUpNote
        } else {
            "Follow up on application for ${application.position}"
        }

        scheduleAlarm(
            requestCode = notificationId,
            applicationId = application.id,
            triggerMillis = triggerMillis,
            title = title,
            message = message
        )
    }

    override fun cancelInterviewReminder(applicationId: Long) {
        val notificationId = NotificationHelper.getInterviewNotificationId(applicationId)
        cancelAlarm(notificationId)
        notificationHelper.cancelNotification(notificationId)
    }

    override fun cancelFollowUpReminder(applicationId: Long) {
        val notificationId = NotificationHelper.getFollowUpNotificationId(applicationId)
        cancelAlarm(notificationId)
        notificationHelper.cancelNotification(notificationId)
    }

    override fun cancelAllRemindersForApplication(applicationId: Long) {
        cancelInterviewReminder(applicationId)
        cancelFollowUpReminder(applicationId)
    }

    override suspend fun cancelAllReminders() {
        val applications = repository.getAllApplications().first()
        applications.forEach { app ->
            cancelAllRemindersForApplication(app.id)
        }
    }

    override suspend fun rescheduleAllFutureReminders() {
        val enabled = userPreferencesRepository.getNotificationsEnabledOnce()
        if (!enabled) return

        val applications = repository.getAllApplications().first()
        applications.forEach { app ->
            if (!app.interviewDate.isNullOrBlank()) {
                scheduleInterviewReminder(app)
            }
            if (!app.followUpDate.isNullOrBlank()) {
                scheduleFollowUpReminder(app)
            }
        }
    }

    private fun scheduleAlarm(
        requestCode: Int,
        applicationId: Long,
        triggerMillis: Long,
        title: String,
        message: String
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_APPLICATION_ID, applicationId)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        fun calculateInterviewTriggerMillis(dateStr: String?, timeStr: String?): Long? {
            if (dateStr.isNullOrBlank()) return null
            return try {
                val date = LocalDate.parse(dateStr.trim())
                val time = parseTime(timeStr)
                val dateTime = LocalDateTime.of(date, time)
                dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                null
            }
        }

        fun calculateFollowUpTriggerMillis(dateStr: String?): Long? {
            if (dateStr.isNullOrBlank()) return null
            return try {
                val date = LocalDate.parse(dateStr.trim())
                // Follow-ups default to 9:00 AM on the specified date
                val time = LocalTime.of(9, 0)
                val dateTime = LocalDateTime.of(date, time)
                dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                null
            }
        }

        private fun parseTime(timeStr: String?): LocalTime {
            if (timeStr.isNullOrBlank()) return LocalTime.of(9, 0)
            val trimmed = timeStr.trim()
            return try {
                LocalTime.parse(trimmed)
            } catch (_: Exception) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                    LocalTime.parse(trimmed.uppercase(Locale.US), formatter)
                } catch (_: Exception) {
                    try {
                        val formatter2 = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
                        LocalTime.parse(trimmed.uppercase(Locale.US), formatter2)
                    } catch (_: Exception) {
                        LocalTime.of(9, 0)
                    }
                }
            }
        }
    }
}
