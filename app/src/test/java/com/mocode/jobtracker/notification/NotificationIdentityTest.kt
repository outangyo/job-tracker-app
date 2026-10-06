package com.mocode.jobtracker.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class NotificationIdentityTest {

    @Test
    fun notificationIds_areDeterministicAndIndependent() {
        val app1Id = 1L
        val app2Id = 2L

        val app1InterviewId = NotificationHelper.getInterviewNotificationId(app1Id)
        val app1FollowUpId = NotificationHelper.getFollowUpNotificationId(app1Id)

        val app2InterviewId = NotificationHelper.getInterviewNotificationId(app2Id)
        val app2FollowUpId = NotificationHelper.getFollowUpNotificationId(app2Id)

        // For the same application, interview and follow-up IDs must be different
        assertNotEquals(app1InterviewId, app1FollowUpId)
        assertNotEquals(app2InterviewId, app2FollowUpId)

        // Across applications, IDs must be unique
        assertNotEquals(app1InterviewId, app2InterviewId)
        assertNotEquals(app1FollowUpId, app2FollowUpId)
        assertNotEquals(app1InterviewId, app2FollowUpId)

        // Deterministic values
        assertEquals(2, app1InterviewId)
        assertEquals(3, app1FollowUpId)
        assertEquals(4, app2InterviewId)
        assertEquals(5, app2FollowUpId)
    }

    @Test
    fun calculateInterviewTriggerMillis_handles24HourAnd12HourFormats() {
        val dateStr = "2030-10-15"
        val time24 = "14:30"
        val time12 = "02:30 PM"

        val millis24 = AndroidReminderScheduler.calculateInterviewTriggerMillis(dateStr, time24)
        val millis12 = AndroidReminderScheduler.calculateInterviewTriggerMillis(dateStr, time12)

        assertNotNull(millis24)
        assertNotNull(millis12)
        // Both 14:30 and 02:30 PM represent the same point in time
        assertEquals(millis24, millis12)

        val instant = Instant.ofEpochMilli(millis24!!)
        val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        assertEquals(LocalDate.of(2030, 10, 15), localDateTime.toLocalDate())
        assertEquals(LocalTime.of(14, 30), localDateTime.toLocalTime())
    }

    @Test
    fun calculateInterviewTriggerMillis_whenTimeIsMissing_defaultsTo9AM() {
        val dateStr = "2030-11-01"
        val millis = AndroidReminderScheduler.calculateInterviewTriggerMillis(dateStr, "")

        assertNotNull(millis)
        val localDateTime = Instant.ofEpochMilli(millis!!).atZone(ZoneId.systemDefault()).toLocalDateTime()
        assertEquals(LocalDate.of(2030, 11, 1), localDateTime.toLocalDate())
        assertEquals(LocalTime.of(9, 0), localDateTime.toLocalTime())
    }

    @Test
    fun calculateInterviewTriggerMillis_whenDateIsInvalidOrBlank_returnsNull() {
        assertNull(AndroidReminderScheduler.calculateInterviewTriggerMillis("", "10:00"))
        assertNull(AndroidReminderScheduler.calculateInterviewTriggerMillis(null, "10:00"))
        assertNull(AndroidReminderScheduler.calculateInterviewTriggerMillis("not-a-date", "10:00"))
    }

    @Test
    fun calculateFollowUpTriggerMillis_schedulesAt9AMOnSpecifiedDate() {
        val dateStr = "2030-12-05"
        val millis = AndroidReminderScheduler.calculateFollowUpTriggerMillis(dateStr)

        assertNotNull(millis)
        val localDateTime = Instant.ofEpochMilli(millis!!).atZone(ZoneId.systemDefault()).toLocalDateTime()
        assertEquals(LocalDate.of(2030, 12, 5), localDateTime.toLocalDate())
        assertEquals(LocalTime.of(9, 0), localDateTime.toLocalTime())
    }

    @Test
    fun calculateFollowUpTriggerMillis_whenDateIsInvalidOrBlank_returnsNull() {
        assertNull(AndroidReminderScheduler.calculateFollowUpTriggerMillis(""))
        assertNull(AndroidReminderScheduler.calculateFollowUpTriggerMillis(null))
        assertNull(AndroidReminderScheduler.calculateFollowUpTriggerMillis("invalid-date"))
    }
}
