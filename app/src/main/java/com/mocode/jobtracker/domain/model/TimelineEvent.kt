package com.mocode.jobtracker.domain.model

data class TimelineEvent(
    val id: Long = 0,
    val applicationId: Long,
    val eventType: TimelineEventType,
    val customTitle: String? = null,
    val eventDate: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayTitle: String
        get() = if (eventType == TimelineEventType.CUSTOM && !customTitle.isNullOrBlank()) {
            customTitle
        } else {
            eventType.displayName
        }
}
