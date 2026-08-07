package com.prayertracker.app.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.prayertracker.app.MainActivity

object Notifications {
    const val CHANNEL_ID = "prayer_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prayer reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Reminds you when each prayer time begins." }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    /**
     * Posts a prayer reminder. [prayerDb] drives the "Prayed on time" quick-action, which logs
     * the prayer from the shade (offline-capable via Room) without opening the app.
     */
    fun show(context: Context, notificationId: Int, title: String, text: String, prayerDb: String) {
        if (!hasPermission(context)) return
        ensureChannel(context)

        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val openApp = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            piFlags,
        )

        val logOnTime = PendingIntent.getBroadcast(
            context,
            notificationId + 10_000,
            Intent(context, ReminderReceiver::class.java)
                .putExtra(PrayerAlarmScheduler.EXTRA_TYPE, PrayerAlarmScheduler.TYPE_LOG)
                .putExtra(PrayerAlarmScheduler.EXTRA_PRAYER, prayerDb)
                .putExtra(PrayerAlarmScheduler.EXTRA_STATUS, "on_time")
                .putExtra(PrayerAlarmScheduler.EXTRA_NOTIF_ID, notificationId),
            piFlags,
        )

        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .addAction(0, "Prayed on time", logOnTime)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, n)
    }

    fun cancel(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
