package com.mocode.jobtracker.domain.model

enum class ApplicationStatus(val displayName: String) {
    WISHLIST("Wishlist"),
    APPLIED("Applied"),
    INTERVIEW("Interview"),
    OFFER("Offer"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn");

    companion object {
        fun fromString(value: String): ApplicationStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: APPLIED
        }
    }
}
