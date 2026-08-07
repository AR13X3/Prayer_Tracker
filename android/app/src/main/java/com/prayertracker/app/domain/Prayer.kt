package com.prayertracker.app.domain

/**
 * Loggable prayers. [db] matches the Postgres `prayer_name` enum. [isFard] marks the five
 * obligatory prayers that count toward streaks; Tahajjud is voluntary and never does.
 *
 * Jummah is not a value here — it's the Friday replacement for Dhuhr (same slot), shown with
 * a "Jummah" label but stored and streak-counted as Dhuhr.
 */
enum class PrayerName(val db: String, val label: String, val isFard: Boolean) {
    FAJR("fajr", "Fajr", true),
    DHUHR("dhuhr", "Dhuhr", true),
    ASR("asr", "Asr", true),
    MAGHRIB("maghrib", "Maghrib", true),
    ISHA("isha", "Isha", true),
    TAHAJJUD("tahajjud", "Tahajjud", false);

    companion object {
        fun fromDb(v: String): PrayerName? = entries.firstOrNull { it.db == v }

        val fard: List<PrayerName> get() = entries.filter { it.isFard }
    }
}

/** [db] matches the Postgres `prayer_status` enum. */
enum class PrayerStatus(val db: String, val label: String) {
    ON_TIME("on_time", "On time"),
    LATE("late", "Late"),
    QADA("qada", "Qada"),
    MISSED("missed", "Missed");

    companion object {
        fun fromDb(v: String): PrayerStatus? = entries.firstOrNull { it.db == v }
    }
}
