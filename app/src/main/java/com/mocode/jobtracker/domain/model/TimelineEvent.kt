package com.mocode.jobtracker.domain.model

data class TimelineEvent(
    val id: Long = 0,
    val applicationId: Long,
    val eventType: TimelineEventType,
    val eventDate: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
