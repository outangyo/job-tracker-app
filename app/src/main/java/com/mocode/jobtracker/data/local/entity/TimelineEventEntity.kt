package com.mocode.jobtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mocode.jobtracker.domain.model.TimelineEventType

@Entity(
    tableName = "timeline_events",
    foreignKeys = [
        ForeignKey(
            entity = ApplicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["applicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["applicationId"])
    ]
)
data class TimelineEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val applicationId: Long,
    val eventType: TimelineEventType,
    val customTitle: String? = null,
    val eventDate: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
