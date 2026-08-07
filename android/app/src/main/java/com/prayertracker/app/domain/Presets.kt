package com.prayertracker.app.domain

/** A manual location preset (no GPS permission needed — see plan §8.2). */
data class CityPreset(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
)

object Presets {
    val cities = listOf(
        CityPreset("Sydney, AU", -33.8688, 151.2093, "Australia/Sydney"),
        CityPreset("Melbourne, AU", -37.8136, 144.9631, "Australia/Melbourne"),
        CityPreset("Brisbane, AU", -27.4698, 153.0251, "Australia/Brisbane"),
        CityPreset("Perth, AU", -31.9523, 115.8613, "Australia/Perth"),
        CityPreset("Adelaide, AU", -34.9285, 138.6007, "Australia/Adelaide"),
        CityPreset("Auckland, NZ", -36.8485, 174.7633, "Pacific/Auckland"),
        CityPreset("London, UK", 51.5074, -0.1278, "Europe/London"),
        CityPreset("New York, US", 40.7128, -74.0060, "America/New_York"),
        CityPreset("Toronto, CA", 43.6532, -79.3832, "America/Toronto"),
        CityPreset("Dubai, AE", 25.2048, 55.2708, "Asia/Dubai"),
        CityPreset("Mecca, SA", 21.4225, 39.8262, "Asia/Riyadh"),
        CityPreset("Istanbul, TR", 41.0082, 28.9784, "Europe/Istanbul"),
        CityPreset("Kuala Lumpur, MY", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        CityPreset("Jakarta, ID", -6.2088, 106.8456, "Asia/Jakarta"),
    )
}

/** Calculation methods, stored as strings that PrayerTimesCalculator matches to adhan2. */
object CalculationMethods {
    // label -> stored value
    val options = linkedMapOf(
        "Muslim World League" to "MuslimWorldLeague",
        "Egyptian" to "Egyptian",
        "Karachi" to "Karachi",
        "Umm al-Qura (Makkah)" to "UmmAlQura",
        "Dubai" to "Dubai",
        "Qatar" to "Qatar",
        "Kuwait" to "Kuwait",
        "Moonsighting Committee" to "MoonsightingCommittee",
        "Singapore" to "Singapore",
        "North America (ISNA)" to "NorthAmerica",
    )

    fun labelFor(stored: String): String =
        options.entries.firstOrNull { it.value.equals(stored, ignoreCase = true) }?.key
            ?: "Muslim World League"
}
