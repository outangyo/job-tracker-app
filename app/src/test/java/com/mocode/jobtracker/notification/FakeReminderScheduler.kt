package com.mocode.jobtracker.notification

import com.mocode.jobtracker.domain.model.Application

class FakeReminderScheduler : ReminderScheduler {

    val scheduledInterviews = mutableListOf<Application>()
    val scheduledFollowUps = mutableListOf<Application>()
    val cancelledInterviews = mutableListOf<Long>()
    val cancelledFollowUps = mutableListOf<Long>()
    val cancelledAllForApp = mutableListOf<Long>()
    var allRemindersCancelled: Boolean = false
    var allRemindersRescheduled: Boolean = false

    override fun scheduleInterviewReminder(application: Application) {
        scheduledInterviews.add(application)
    }

    override fun scheduleFollowUpReminder(application: Application) {
        scheduledFollowUps.add(application)
    }

    override fun cancelInterviewReminder(applicationId: Long) {
        cancelledInterviews.add(applicationId)
    }

    override fun cancelFollowUpReminder(applicationId: Long) {
        cancelledFollowUps.add(applicationId)
    }

    override fun cancelAllRemindersForApplication(applicationId: Long) {
        cancelledAllForApp.add(applicationId)
        cancelInterviewReminder(applicationId)
        cancelFollowUpReminder(applicationId)
    }

    override suspend fun cancelAllReminders() {
        allRemindersCancelled = true
    }

    override suspend fun rescheduleAllFutureReminders() {
        allRemindersRescheduled = true
    }

    fun clear() {
        scheduledInterviews.clear()
        scheduledFollowUps.clear()
        cancelledInterviews.clear()
        cancelledFollowUps.clear()
        cancelledAllForApp.clear()
        allRemindersCancelled = false
        allRemindersRescheduled = false
    }
}
