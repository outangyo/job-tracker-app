package com.mocode.jobtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mocode.jobtracker.domain.model.ApplicationStatus

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val position: String,
    val jobUrl: String? = null,
    val location: String? = null,
    val salary: String? = null,
    val appliedDate: String,
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val interviewDate: String? = null,
    val interviewTime: String? = null,
    val interviewRound: String? = null,
    val interviewType: String? = null,
    val interviewNotes: String? = null,
    val followUpDate: String? = null,
    val followUpNote: String? = null,
    val generalNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
