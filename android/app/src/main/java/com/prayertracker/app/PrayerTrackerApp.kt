package com.prayertracker.app

import android.app.Application
import com.prayertracker.app.data.Supabase
import com.prayertracker.app.data.local.LocalDb
import com.prayertracker.app.reminders.Notifications
import com.prayertracker.app.reminders.PrayerAlarmScheduler

class PrayerTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LocalDb.init(this)                        // offline cache before anything reads it
        Supabase.client                           // Auth plugin starts restoring the session
        Notifications.ensureChannel(this)
        PrayerAlarmScheduler.reschedule(this)     // re-arm reminders from cached location
    }
}
