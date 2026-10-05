package com.mocode.jobtracker.data.local.mapper

import com.mocode.jobtracker.data.local.entity.ApplicationEntity
import com.mocode.jobtracker.data.local.entity.TimelineEventEntity
import com.mocode.jobtracker.domain.model.Application
import com.mocode.jobtracker.domain.model.TimelineEvent

fun ApplicationEntity.toDomain(): Application {
    return Application(
        id = id,
        companyName = companyName,
        position = position,
        jobUrl = jobUrl,
        location = location,
        salary = salary,
        appliedDate = appliedDate,
        status = status,
        interviewDate = interviewDate,
        interviewTime = interviewTime,
        interviewRound = interviewRound,
        interviewType = interviewType,
        interviewNotes = interviewNotes,
        followUpDate = followUpDate,
        followUpNote = followUpNote,
        generalNotes = generalNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Application.toEntity(): ApplicationEntity {
    return ApplicationEntity(
        id = id,
        companyName = companyName,
        position = position,
        jobUrl = jobUrl,
        location = location,
        salary = salary,
        appliedDate = appliedDate,
        status = status,
        interviewDate = interviewDate,
        interviewTime = interviewTime,
        interviewRound = interviewRound,
        interviewType = interviewType,
        interviewNotes = interviewNotes,
        followUpDate = followUpDate,
        followUpNote = followUpNote,
        generalNotes = generalNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TimelineEventEntity.toDomain(): TimelineEvent {
    return TimelineEvent(
        id = id,
        applicationId = applicationId,
        eventType = eventType,
        customTitle = customTitle,
        eventDate = eventDate,
        note = note,
        createdAt = createdAt
    )
}

fun TimelineEvent.toEntity(): TimelineEventEntity {
    return TimelineEventEntity(
        id = id,
        applicationId = applicationId,
        eventType = eventType,
        customTitle = customTitle,
        eventDate = eventDate,
        note = note,
        createdAt = createdAt
    )
}
