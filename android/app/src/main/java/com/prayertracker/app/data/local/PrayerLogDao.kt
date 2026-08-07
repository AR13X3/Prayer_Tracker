package com.prayertracker.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PrayerLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PrayerLogEntity)

    /** Live (non-deleted) logs for a day. */
    @Query("SELECT * FROM prayer_logs WHERE userId = :userId AND prayerDate = :date AND deleted = 0")
    suspend fun forDate(userId: String, date: String): List<PrayerLogEntity>

    /** Everything awaiting a push (inserts, updates, and tombstones). */
    @Query("SELECT * FROM prayer_logs WHERE pendingSync = 1")
    suspend fun pending(): List<PrayerLogEntity>

    @Query("UPDATE prayer_logs SET pendingSync = 0 WHERE userId = :userId AND prayerDate = :date AND prayer = :prayer")
    suspend fun markSynced(userId: String, date: String, prayer: String)

    /** Removes a tombstone row once its server delete has succeeded. */
    @Query("DELETE FROM prayer_logs WHERE userId = :userId AND prayerDate = :date AND prayer = :prayer AND deleted = 1")
    suspend fun purgeTombstone(userId: String, date: String, prayer: String)

    @Query("DELETE FROM prayer_logs WHERE userId = :userId")
    suspend fun clearUser(userId: String)
}
