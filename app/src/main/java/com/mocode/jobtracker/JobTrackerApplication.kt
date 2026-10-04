package com.mocode.jobtracker

import android.app.Application
import com.mocode.jobtracker.data.local.JobTrackerDatabase
import com.mocode.jobtracker.data.repository.JobRepository
import com.mocode.jobtracker.data.repository.RoomJobRepository

class JobTrackerApplication : Application() {

    val database: JobTrackerDatabase by lazy {
        JobTrackerDatabase.getDatabase(this)
    }

    val repository: JobRepository by lazy {
        RoomJobRepository(
            applicationDao = database.applicationDao(),
            timelineEventDao = database.timelineEventDao()
        )
    }
}
