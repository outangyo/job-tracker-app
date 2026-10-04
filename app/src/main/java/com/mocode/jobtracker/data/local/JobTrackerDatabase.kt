package com.mocode.jobtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mocode.jobtracker.data.local.converter.Converters
import com.mocode.jobtracker.data.local.dao.ApplicationDao
import com.mocode.jobtracker.data.local.dao.TimelineEventDao
import com.mocode.jobtracker.data.local.entity.ApplicationEntity
import com.mocode.jobtracker.data.local.entity.TimelineEventEntity

@Database(
    entities = [
        ApplicationEntity::class,
        TimelineEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JobTrackerDatabase : RoomDatabase() {

    abstract fun applicationDao(): ApplicationDao
    abstract fun timelineEventDao(): TimelineEventDao

    companion object {
        private const val DATABASE_NAME = "job_tracker_database"

        @Volatile
        private var INSTANCE: JobTrackerDatabase? = null

        fun getDatabase(context: Context): JobTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JobTrackerDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
