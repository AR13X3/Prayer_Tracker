package com.prayertracker.app.data

import com.prayertracker.app.data.local.LocalDb
import com.prayertracker.app.data.local.PrayerLogDao
import com.prayertracker.app.data.local.PrayerLogEntity
import com.prayertracker.app.data.model.PrayerLogRow
import com.prayertracker.app.data.model.PrayerLogUpsert
import com.prayertracker.app.domain.PrayerName
import com.prayertracker.app.domain.PrayerStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.LocalDate

/**
 * Offline-first prayer logging (plan §7.2). Room is the source of truth for the UI; an outbox
 * pushes local changes to Supabase opportunistically. A log made with no signal is never lost —
 * it stays `pendingSync` and pushes on the next successful call. The DB's
 * `unique(user_id, prayer_date, prayer)` makes the push idempotent.
 *
 * [date] is the user's LOCAL date, formatted yyyy-MM-dd (plan §7.1).
 */
class PrayerLogRepository(
    private val client: SupabaseClient = Supabase.client,
    private val dao: PrayerLogDao = LocalDb.prayerLogs,
) {
    private fun uid(): String? = client.auth.currentUserOrNull()?.id

    suspend fun logsForDate(date: LocalDate): List<PrayerLogRow> {
        val userId = uid() ?: return emptyList()
        runCatching { pushPending() }            // best-effort; offline is fine
        runCatching { pullDate(userId, date) }
        return dao.forDate(userId, date.toString())
            .map { PrayerLogRow(prayer = it.prayer, status = it.status, inJamaah = it.inJamaah) }
    }

    suspend fun upsert(date: LocalDate, prayer: PrayerName, status: PrayerStatus, inJamaah: Boolean) {
        val userId = uid() ?: error("Not authenticated")
        writeLocal(userId, date, prayer.db, status.db, inJamaah, deleted = false)
        runCatching { pushPending() }
    }

    suspend fun clear(date: LocalDate, prayer: PrayerName) {
        val userId = uid() ?: return
        writeLocal(userId, date, prayer.db, status = "", inJamaah = false, deleted = true)
        runCatching { pushPending() }
    }

    /** Used by the notification quick-action, where the session may not be loaded yet. */
    suspend fun logFromNotification(userId: String, date: LocalDate, prayer: PrayerName, status: PrayerStatus) {
        writeLocal(userId, date, prayer.db, status.db, inJamaah = false, deleted = false)
        runCatching { pushPending() }
    }

    /** Clears the local cache (e.g. on sign-out) so another account can't see stale logs. */
    suspend fun clearLocal() {
        val userId = uid() ?: return
        dao.clearUser(userId)
    }

    private suspend fun writeLocal(
        userId: String,
        date: LocalDate,
        prayer: String,
        status: String,
        inJamaah: Boolean,
        deleted: Boolean,
    ) {
        dao.upsert(
            PrayerLogEntity(
                userId = userId,
                prayerDate = date.toString(),
                prayer = prayer,
                status = status,
                inJamaah = inJamaah,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true,
                deleted = deleted,
            ),
        )
    }

    private suspend fun pushPending() {
        for (e in dao.pending()) {
            if (e.deleted) {
                client.from("prayer_logs").delete {
                    filter {
                        eq("user_id", e.userId); eq("prayer_date", e.prayerDate); eq("prayer", e.prayer)
                    }
                }
                dao.purgeTombstone(e.userId, e.prayerDate, e.prayer)
            } else {
                client.from("prayer_logs").upsert(
                    PrayerLogUpsert(e.userId, e.prayerDate, e.prayer, e.status, e.inJamaah),
                ) { onConflict = "user_id,prayer_date,prayer" }
                dao.markSynced(e.userId, e.prayerDate, e.prayer)
            }
        }
    }

    private suspend fun pullDate(userId: String, date: LocalDate) {
        val serverRows = client.from("prayer_logs")
            .select(Columns.list("prayer", "status", "in_jamaah")) {
                filter { eq("user_id", userId); eq("prayer_date", date.toString()) }
            }
            .decodeList<PrayerLogRow>()
        // Don't clobber local changes that haven't been pushed yet.
        val pendingThisDate = dao.pending()
            .filter { it.prayerDate == date.toString() }
            .map { it.prayer }
            .toSet()
        for (r in serverRows) {
            if (r.prayer in pendingThisDate) continue
            dao.upsert(
                PrayerLogEntity(
                    userId = userId,
                    prayerDate = date.toString(),
                    prayer = r.prayer,
                    status = r.status,
                    inJamaah = r.inJamaah,
                    updatedAt = System.currentTimeMillis(),
                    pendingSync = false,
                    deleted = false,
                ),
            )
        }
    }
}
