package com.mocode.jobtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mocode.jobtracker.data.local.converter.Converters
import com.mocode.jobtracker.data.local.dao.ApplicationDao
import com.mocode.jobtracker.data.local.dao.TimelineEventDao
import com.mocode.jobtracker.data.local.entity.ApplicationEntity
import com.mocode.jobtracker.data.local.entity.TimelineEventEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE timeline_events ADD COLUMN customTitle TEXT")
    }
}

@Database(
    entities = [
        ApplicationEntity::class,
        TimelineEventEntity::class
    ],
    version = 2,
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
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
