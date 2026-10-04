package com.mocode.jobtracker.data.local.converter

import androidx.room.TypeConverter
import com.mocode.jobtracker.domain.model.ApplicationStatus
import com.mocode.jobtracker.domain.model.TimelineEventType

class Converters {
    @TypeConverter
    fun fromApplicationStatus(status: ApplicationStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toApplicationStatus(value: String?): ApplicationStatus? {
        return value?.let { ApplicationStatus.fromString(it) }
    }

    @TypeConverter
    fun fromTimelineEventType(type: TimelineEventType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTimelineEventType(value: String?): TimelineEventType? {
        return value?.let { TimelineEventType.fromString(it) }
    }
}
