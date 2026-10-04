package com.mocode.jobtracker.data.local

import com.mocode.jobtracker.data.local.entity.ApplicationEntity
import com.mocode.jobtracker.data.local.entity.TimelineEventEntity
import com.mocode.jobtracker.data.local.mapper.toDomain
import com.mocode.jobtracker.data.local.mapper.toEntity
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.domain.model.TimelineEvent
import com.mocode.jobtracker.domain.model.TimelineEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EntityMappersTest {

    @Test
    fun application_domainToEntityAndBack_preservesAllFields() {
        val originalDomain = Application(
            id = 42L,
            companyName = "Google",
            position = "Senior Android Engineer",
            jobUrl = "https://careers.google.com/jobs/123",
            location = "Bangkok (Hybrid)",
            salary = "150,000 THB",
            appliedDate = "2026-10-04",
            status = ApplicationStatus.INTERVIEW,
            interviewDate = "2026-10-15",
            interviewTime = "10:00 AM",
            interviewRound = "System Design",
            interviewType = "Google Meet",
            interviewNotes = "Review architecture and Compose internals",
            followUpDate = "2026-10-20",
            followUpNote = "Send follow-up email if no news",
            generalNotes = "Applied through referral",
            createdAt = 1728000000000L,
            updatedAt = 1728000500000L
        )

        val entity = originalDomain.toEntity()
        val mappedDomain = entity.toDomain()

        assertEquals(originalDomain.id, mappedDomain.id)
        assertEquals(originalDomain.companyName, mappedDomain.companyName)
        assertEquals(originalDomain.position, mappedDomain.position)
        assertEquals(originalDomain.jobUrl, mappedDomain.jobUrl)
        assertEquals(originalDomain.location, mappedDomain.location)
        assertEquals(originalDomain.salary, mappedDomain.salary)
        assertEquals(originalDomain.appliedDate, mappedDomain.appliedDate)
        assertEquals(originalDomain.status, mappedDomain.status)
        assertEquals(originalDomain.interviewDate, mappedDomain.interviewDate)
        assertEquals(originalDomain.interviewTime, mappedDomain.interviewTime)
        assertEquals(originalDomain.interviewRound, mappedDomain.interviewRound)
        assertEquals(originalDomain.interviewType, mappedDomain.interviewType)
        assertEquals(originalDomain.interviewNotes, mappedDomain.interviewNotes)
        assertEquals(originalDomain.followUpDate, mappedDomain.followUpDate)
        assertEquals(originalDomain.followUpNote, mappedDomain.followUpNote)
        assertEquals(originalDomain.generalNotes, mappedDomain.generalNotes)
        assertEquals(originalDomain.createdAt, mappedDomain.createdAt)
        assertEquals(originalDomain.updatedAt, mappedDomain.updatedAt)
    }

    @Test
    fun timelineEvent_domainToEntityAndBack_preservesAllFields() {
        val originalEvent = TimelineEvent(
            id = 10L,
            applicationId = 42L,
            eventType = TimelineEventType.INTERVIEW_SCHEDULED,
            eventDate = "2026-10-05",
            note = "Scheduled round 1 with tech lead",
            createdAt = 1728000100000L
        )

        val entity = originalEvent.toEntity()
        val mappedEvent = entity.toDomain()

        assertEquals(originalEvent.id, mappedEvent.id)
        assertEquals(originalEvent.applicationId, mappedEvent.applicationId)
        assertEquals(originalEvent.eventType, mappedEvent.eventType)
        assertEquals(originalEvent.eventDate, mappedEvent.eventDate)
        assertEquals(originalEvent.note, mappedEvent.note)
        assertEquals(originalEvent.createdAt, mappedEvent.createdAt)
    }
}
