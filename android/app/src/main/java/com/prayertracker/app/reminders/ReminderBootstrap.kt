package com.prayertracker.app.reminders

import android.content.Context
import com.prayertracker.app.data.ProfileRepository

/**
 * Bridges the online profile to the offline reminder scheduler: fetches the profile once,
 * caches the location into [ReminderPrefs], then (re)schedules alarms. Called after login
 * (from MainScreen) and after the user saves Settings. If the profile has no location yet,
 * nothing is scheduled — the user hasn't picked a city.
 */
object ReminderBootstrap {

    suspend fun sync(context: Context, profileRepo: ProfileRepository = ProfileRepository()) {
        val profile = runCatching { profileRepo.getMyProfile() }.getOrNull() ?: return
        val prefs = ReminderPrefs(context)
        prefs.userId = profile.id
        val lat = profile.latitude
        val lng = profile.longitude
        if (lat != null && lng != null) {
            prefs.cacheLocation(
                lat = lat,
                lng = lng,
                method = profile.calculationMethod,
                madhab = profile.madhab,
            )
        }
        PrayerAlarmScheduler.reschedule(context)
    }

    /** Schedules from whatever is already cached — no network. Safe to call on app start. */
    fun rescheduleFromCache(context: Context) {
        PrayerAlarmScheduler.reschedule(context)
    }
}
