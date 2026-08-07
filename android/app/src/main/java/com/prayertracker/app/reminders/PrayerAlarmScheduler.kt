package com.prayertracker.app.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.prayertracker.app.domain.PrayerName
import com.prayertracker.app.prayer.PrayerTimesCalculator
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Schedules one exact alarm per enabled prayer for today, plus a self-perpetuating daily
 * "refresh" alarm just after midnight that re-runs this for the new day (times shift daily).
 * Reads only [ReminderPrefs] — never the network — so it works offline and after reboot.
 */
object PrayerAlarmScheduler {

    private const val REQUEST_REFRESH = 100
    const val EXTRA_TYPE = "type"
    const val EXTRA_PRAYER = "prayer"
    const val EXTRA_STATUS = "status"
    const val EXTRA_NOTIF_ID = "notif_id"
    const val TYPE_PRAYER = "prayer"
    const val TYPE_REFRESH = "refresh"
    const val TYPE_LOG = "log"           // notification quick-action

    fun reschedule(context: Context) {
        val prefs = ReminderPrefs(context)
        val am = context.getSystemService(AlarmManager::class.java) ?: return

        // Always clear existing prayer alarms first so disabled ones stop firing.
        PrayerName.entries.forEach { cancelPrayer(context, am, it) }

        val loc = prefs.cachedLocation()
        if (prefs.masterEnabled && loc != null) {
            val zone = runCatching { ZoneId.of(loc.timezone) }.getOrDefault(ZoneId.systemDefault())
            val today = LocalDate.now(zone)
            val times = PrayerTimesCalculator.compute(loc.lat, loc.lng, today, loc.method, loc.madhab)
            val now = System.currentTimeMillis()

            PrayerName.entries.forEach { prayer ->
                val at = times.getValue(prayer)
                if (prefs.isEnabled(prayer) && at > now) {
                    scheduleExact(context, am, prayer.ordinal, at, prayerIntent(context, prayer))
                }
            }
        }

        // Daily refresh at 00:05 local, regardless of master (cheap; it re-checks master).
        scheduleDailyRefresh(context, am)
    }

    fun cancelAll(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        PrayerName.entries.forEach { cancelPrayer(context, am, it) }
    }

    private fun scheduleDailyRefresh(context: Context, am: AlarmManager) {
        val zone = ZoneId.systemDefault()
        val next = LocalDate.now(zone).plusDays(1).atTime(LocalTime.of(0, 5))
            .atZone(zone).toInstant().toEpochMilli()
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_REFRESH,
            Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_TYPE, TYPE_REFRESH),
            piFlags(),
        )
        scheduleExactPi(am, next, pi)
    }

    private fun prayerIntent(context: Context, prayer: PrayerName): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            prayer.ordinal,
            Intent(context, ReminderReceiver::class.java)
                .putExtra(EXTRA_TYPE, TYPE_PRAYER)
                .putExtra(EXTRA_PRAYER, prayer.db),
            piFlags(),
        )

    private fun cancelPrayer(context: Context, am: AlarmManager, prayer: PrayerName) {
        am.cancel(prayerIntent(context, prayer))
    }

    private fun scheduleExact(context: Context, am: AlarmManager, requestCode: Int, triggerAt: Long, pi: PendingIntent) {
        scheduleExactPi(am, triggerAt, pi)
    }

    private fun scheduleExactPi(am: AlarmManager, triggerAt: Long, pi: PendingIntent) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            // Degrade gracefully: inexact but still Doze-tolerant.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun piFlags(): Int =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
}
