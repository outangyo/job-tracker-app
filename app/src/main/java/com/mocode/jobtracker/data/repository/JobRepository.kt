package com.mocode.jobtracker.data.repository

import com.mocode.jobtracker.domain.model.Application
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface contract establishing the data layer architecture for JobTracker.
 * Concrete Room/local implementation will be provided in Milestone 2.
 */
interface JobRepository {
    fun getAllApplications(): Flow<List<Application>>
    suspend fun getApplicationById(id: Long): Application?
    suspend fun insertApplication(application: Application): Long
    suspend fun updateApplication(application: Application)
    suspend fun deleteApplication(application: Application)
}
