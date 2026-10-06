package com.mocode.jobtracker.notification

import com.mocode.jobtracker.domain.model.Application

interface ReminderScheduler {
    fun scheduleInterviewReminder(application: Application)
    fun scheduleFollowUpReminder(application: Application)
    fun cancelInterviewReminder(applicationId: Long)
    fun cancelFollowUpReminder(applicationId: Long)
    fun cancelAllRemindersForApplication(applicationId: Long)
    suspend fun cancelAllReminders()
    suspend fun rescheduleAllFutureReminders()
}
