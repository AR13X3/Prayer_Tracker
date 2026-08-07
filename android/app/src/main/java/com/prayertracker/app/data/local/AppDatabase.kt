package com.prayertracker.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PrayerLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerLogDao(): PrayerLogDao
}

/**
 * Process-wide database singleton. Initialised once in PrayerTrackerApp.onCreate so
 * repositories (which have no Context) and broadcast receivers can reach the DAO.
 */
object LocalDb {
    @Volatile private var instance: AppDatabase? = null

    fun init(context: Context) {
        if (instance == null) synchronized(this) {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prayertracker.db",
                ).build()
            }
        }
    }

    val prayerLogs: PrayerLogDao
        get() = requireNotNull(instance) { "LocalDb.init() not called" }.prayerLogDao()
}
