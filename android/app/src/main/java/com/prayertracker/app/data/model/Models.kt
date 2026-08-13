package com.prayertracker.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A row of public.profiles (only the columns the app reads). */
@Serializable
data class Profile(
    val id: String,
    @SerialName("display_name") val displayName: String,
    // The user's last-known device zone, written back by the app. Nothing renders from it —
    // display and alarms read ZoneId.systemDefault() — it's here for friend-facing views.
    val timezone: String = "UTC",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("city_label") val cityLabel: String? = null,
    @SerialName("calculation_method") val calculationMethod: String = "MuslimWorldLeague",
    val madhab: String = "shafi",
)

/** A prayer_logs row as read for the Today screen. */
@Serializable
data class PrayerLogRow(
    val prayer: String,
    val status: String,
    @SerialName("in_jamaah") val inJamaah: Boolean = false,
)

/** Payload for upserting a prayer_logs row (idempotent on the unique key). */
@Serializable
data class PrayerLogUpsert(
    @SerialName("user_id") val userId: String,
    @SerialName("prayer_date") val prayerDate: String,
    val prayer: String,
    val status: String,
    @SerialName("in_jamaah") val inJamaah: Boolean,
)
