package com.mocode.jobtracker.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mocode.jobtracker.data.local.dao.ApplicationDao
import com.mocode.jobtracker.data.local.entity.ApplicationEntity
import com.mocode.jobtracker.domain.model.ApplicationStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class ApplicationDaoTest {

    private lateinit var database: JobTrackerDatabase
    private lateinit var applicationDao: ApplicationDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, JobTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        applicationDao = database.applicationDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryApplicationById() = runTest {
        val entity = ApplicationEntity(
            companyName = "Agoda",
            position = "Android Developer",
            appliedDate = "2026-10-01",
            status = ApplicationStatus.APPLIED,
            salary = "90,000 THB"
        )

        val insertedId = applicationDao.insert(entity)
        val loaded = applicationDao.getApplicationByIdOnce(insertedId)

        assertNotNull(loaded)
        assertEquals(insertedId, loaded?.id)
        assertEquals("Agoda", loaded?.companyName)
        assertEquals("Android Developer", loaded?.position)
        assertEquals(ApplicationStatus.APPLIED, loaded?.status)
    }

    @Test
    fun getAllApplications_returnsAllInsertedInOrder() = runTest {
        val app1 = ApplicationEntity(
            companyName = "Google",
            position = "Android Engineer",
            appliedDate = "2026-10-01",
            status = ApplicationStatus.INTERVIEW,
            updatedAt = 1000L
        )
        val app2 = ApplicationEntity(
            companyName = "Apple",
            position = "iOS Developer",
            appliedDate = "2026-10-02",
            status = ApplicationStatus.APPLIED,
            updatedAt = 2000L
        )

        applicationDao.insert(app1)
        applicationDao.insert(app2)

        val allApps = applicationDao.getAllApplications().first()
        assertEquals(2, allApps.size)
        // Descending by updatedAt
        assertEquals("Apple", allApps[0].companyName)
        assertEquals("Google", allApps[1].companyName)
    }

    @Test
    fun updateApplication_persistsChangesCorrectly() = runTest {
        val app = ApplicationEntity(
            companyName = "Line",
            position = "Software Engineer",
            appliedDate = "2026-10-01",
            status = ApplicationStatus.APPLIED
        )
        val id = applicationDao.insert(app)

        val toUpdate = app.copy(
            id = id,
            status = ApplicationStatus.INTERVIEW,
            interviewDate = "2026-10-12",
            interviewRound = "Round 1"
        )
        applicationDao.update(toUpdate)

        val updated = applicationDao.getApplicationByIdOnce(id)
        assertNotNull(updated)
        assertEquals(ApplicationStatus.INTERVIEW, updated?.status)
        assertEquals("2026-10-12", updated?.interviewDate)
        assertEquals("Round 1", updated?.interviewRound)
    }

    @Test
    fun deleteApplication_removesFromDatabase() = runTest {
        val app = ApplicationEntity(
            companyName = "Shopee",
            position = "Mobile Engineer",
            appliedDate = "2026-10-01"
        )
        val id = applicationDao.insert(app)
        val inserted = applicationDao.getApplicationByIdOnce(id)
        assertNotNull(inserted)

        applicationDao.delete(inserted!!)
        val afterDelete = applicationDao.getApplicationByIdOnce(id)
        assertNull(afterDelete)
    }

    @Test
    fun deleteById_removesApplication() = runTest {
        val app = ApplicationEntity(
            companyName = "Grab",
            position = "Android Lead",
            appliedDate = "2026-10-01"
        )
        val id = applicationDao.insert(app)
        assertEquals(1, applicationDao.getCount())

        applicationDao.deleteById(id)
        assertEquals(0, applicationDao.getCount())
    }
}
