package com.prayertracker.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prayertracker.app.data.PrayerLogRepository
import com.prayertracker.app.domain.PrayerName
import com.prayertracker.app.domain.PrayerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * Handles three alarm/action kinds:
 *  - TYPE_PRAYER  -> post a "time for {prayer}" notification (with a quick-action).
 *  - TYPE_REFRESH -> recompute and reschedule the day's alarms.
 *  - TYPE_LOG     -> the "Prayed on time" quick-action: log offline via Room, then dismiss.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TYPE)) {
            PrayerAlarmScheduler.TYPE_REFRESH -> PrayerAlarmScheduler.reschedule(context)

            PrayerAlarmScheduler.TYPE_PRAYER -> {
                val prayer = PrayerName.fromDb(intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER) ?: return) ?: return
                val label = if (prayer == PrayerName.DHUHR && LocalDate.now().dayOfWeek == DayOfWeek.FRIDAY) "Jummah" else prayer.label
                Notifications.show(
                    context = context,
                    notificationId = 1000 + prayer.ordinal,
                    title = "$label time",
                    text = "It's time for $label. Tap to open, or log it right here.",
                    prayerDb = prayer.db,
                )
            }

            PrayerAlarmScheduler.TYPE_LOG -> {
                val prayer = PrayerName.fromDb(intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER) ?: return) ?: return
                val status = PrayerStatus.fromDb(intent.getStringExtra(PrayerAlarmScheduler.EXTRA_STATUS) ?: "on_time")
                    ?: PrayerStatus.ON_TIME
                val notifId = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_NOTIF_ID, 1000 + prayer.ordinal)
                val userId = ReminderPrefs(context).userId ?: return  // not signed in on this device
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)

                // Keep the process alive while the offline write (+ best-effort push) runs.
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        PrayerLogRepository().logFromNotification(userId, today, prayer, status)
                    } finally {
                        Notifications.cancel(context, notifId)
                        pending.finish()
                    }
                }
            }
        }
    }
}
