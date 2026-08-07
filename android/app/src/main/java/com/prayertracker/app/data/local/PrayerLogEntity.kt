package com.prayertracker.app.data.local

import androidx.room.Entity

/**
 * Local source of truth for prayer logs (plan §7.2). Writes land here instantly; an outbox
 * pushes them to Supabase when possible.
 *
 * - [pendingSync] = has a local change not yet confirmed on the server.
 * - [deleted] = a tombstone for a "clear" that still needs to be pushed as a server delete.
 * - [updatedAt] = epoch millis of the last local change (last-write-wins hint).
 */
@Entity(tableName = "prayer_logs", primaryKeys = ["userId", "prayerDate", "prayer"])
data class PrayerLogEntity(
    val userId: String,
    val prayerDate: String,   // yyyy-MM-dd, the user's local date
    val prayer: String,       // PrayerName.db
    val status: String,       // PrayerStatus.db (ignored when deleted = true)
    val inJamaah: Boolean,
    val updatedAt: Long,
    val pendingSync: Boolean,
    val deleted: Boolean,
)
