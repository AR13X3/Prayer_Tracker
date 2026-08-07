package com.prayertracker.app.reminders

import android.content.Context
import com.prayertracker.app.domain.PrayerName

/**
 * Device-local reminder settings + a cached copy of the location needed to compute prayer
 * times offline. The database never stores *when* to remind (plan §2.2) — only what was
 * logged — so all of this lives in SharedPreferences.
 */
class ReminderPrefs(context: Context) {

    private val sp = context.applicationContext.getSharedPreferences("reminders", Context.MODE_PRIVATE)

    var masterEnabled: Boolean
        get() = sp.getBoolean("master", true)
        set(v) = sp.edit().putBoolean("master", v).apply()

    /** Cached signed-in user id, so the notification quick-action can log offline. */
    var userId: String?
        get() = sp.getString("user_id", null)
        set(v) = sp.edit().putString("user_id", v).apply()

    // Fard reminders default on; Tahajjud (voluntary, middle of the night) defaults off.
    fun isEnabled(prayer: PrayerName): Boolean = sp.getBoolean("prayer_${prayer.db}", prayer.isFard)

    fun setEnabled(prayer: PrayerName, enabled: Boolean) {
        sp.edit().putBoolean("prayer_${prayer.db}", enabled).apply()
    }

    /** Cached location so the alarm scheduler never needs the network. */
    fun cacheLocation(lat: Double, lng: Double, method: String, madhab: String, timezone: String) {
        sp.edit()
            .putString("lat", lat.toString())
            .putString("lng", lng.toString())
            .putString("method", method)
            .putString("madhab", madhab)
            .putString("timezone", timezone)
            .apply()
    }

    data class CachedLocation(
        val lat: Double,
        val lng: Double,
        val method: String,
        val madhab: String,
        val timezone: String,
    )

    fun cachedLocation(): CachedLocation? {
        val lat = sp.getString("lat", null)?.toDoubleOrNull() ?: return null
        val lng = sp.getString("lng", null)?.toDoubleOrNull() ?: return null
        return CachedLocation(
            lat = lat,
            lng = lng,
            method = sp.getString("method", "MuslimWorldLeague")!!,
            madhab = sp.getString("madhab", "shafi")!!,
            timezone = sp.getString("timezone", "Australia/Sydney")!!,
        )
    }
}
