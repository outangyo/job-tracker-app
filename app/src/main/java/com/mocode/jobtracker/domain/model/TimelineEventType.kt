package com.mocode.jobtracker.domain.model

enum class TimelineEventType(val displayName: String) {
    APPLIED("Applied"),
    HR_CONTACTED("HR Contacted"),
    INTERVIEW_SCHEDULED("Interview Scheduled"),
    INTERVIEW_COMPLETED("Interview Completed"),
    FOLLOW_UP_SENT("Follow-up Sent"),
    OFFER_RECEIVED("Offer Received"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn"),
    CUSTOM("Custom");

    companion object {
        fun fromString(value: String): TimelineEventType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: CUSTOM
        }
    }
}
