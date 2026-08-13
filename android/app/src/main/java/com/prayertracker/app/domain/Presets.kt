package com.prayertracker.app.domain

/**
 * A manual location preset (no GPS permission needed — see plan §8.2).
 *
 * Deliberately carries no timezone. Prayer times are computed from lat/lng as absolute
 * instants and rendered in the *device's* zone, so choosing a city sets where you pray —
 * never which clock you read it on.
 */
data class CityPreset(
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

object Presets {

    /**
     * Grouped by region for a sensible default scroll order. Labels are "City, CC" because
     * the country code is what people type into [search].
     */
    val cities = listOf(
        // ── South Asia ────────────────────────────────────────────────────
        CityPreset("Dhaka, BD", 23.8103, 90.4125),
        CityPreset("Sylhet, BD", 24.8949, 91.8687),
        CityPreset("Chattogram, BD", 22.3569, 91.7832),
        CityPreset("Khulna, BD", 22.8456, 89.5403),
        CityPreset("Rajshahi, BD", 24.3745, 88.6042),
        CityPreset("Karachi, PK", 24.8607, 67.0011),
        CityPreset("Lahore, PK", 31.5204, 74.3587),
        CityPreset("Islamabad, PK", 33.6844, 73.0479),
        CityPreset("Peshawar, PK", 34.0151, 71.5249),
        CityPreset("Delhi, IN", 28.6139, 77.2090),
        CityPreset("Mumbai, IN", 19.0760, 72.8777),
        CityPreset("Kolkata, IN", 22.5726, 88.3639),
        CityPreset("Hyderabad, IN", 17.3850, 78.4867),
        CityPreset("Bengaluru, IN", 12.9716, 77.5946),
        CityPreset("Chennai, IN", 13.0827, 80.2707),
        CityPreset("Colombo, LK", 6.9271, 79.8612),
        CityPreset("Kathmandu, NP", 27.7172, 85.3240),
        CityPreset("Kabul, AF", 34.5553, 69.2075),

        // ── Middle East ───────────────────────────────────────────────────
        CityPreset("Mecca, SA", 21.4225, 39.8262),
        CityPreset("Medina, SA", 24.4686, 39.6142),
        CityPreset("Riyadh, SA", 24.7136, 46.6753),
        CityPreset("Jeddah, SA", 21.4858, 39.1925),
        CityPreset("Dubai, AE", 25.2048, 55.2708),
        CityPreset("Abu Dhabi, AE", 24.4539, 54.3773),
        CityPreset("Doha, QA", 25.2854, 51.5310),
        CityPreset("Kuwait City, KW", 29.3759, 47.9774),
        CityPreset("Manama, BH", 26.2285, 50.5860),
        CityPreset("Muscat, OM", 23.5880, 58.3829),
        CityPreset("Amman, JO", 31.9454, 35.9284),
        CityPreset("Jerusalem, PS", 31.7683, 35.2137),
        CityPreset("Baghdad, IQ", 33.3152, 44.3661),
        CityPreset("Tehran, IR", 35.6892, 51.3890),
        CityPreset("Istanbul, TR", 41.0082, 28.9784),
        CityPreset("Ankara, TR", 39.9334, 32.8597),

        // ── Southeast & East Asia ─────────────────────────────────────────
        CityPreset("Kuala Lumpur, MY", 3.1390, 101.6869),
        CityPreset("Singapore, SG", 1.3521, 103.8198),
        CityPreset("Jakarta, ID", -6.2088, 106.8456),
        CityPreset("Bandung, ID", -6.9175, 107.6191),
        CityPreset("Manila, PH", 14.5995, 120.9842),
        CityPreset("Bangkok, TH", 13.7563, 100.5018),
        CityPreset("Tashkent, UZ", 41.2995, 69.2401),

        // ── Africa ────────────────────────────────────────────────────────
        CityPreset("Cairo, EG", 30.0444, 31.2357),
        CityPreset("Khartoum, SD", 15.5007, 32.5599),
        CityPreset("Casablanca, MA", 33.5731, -7.5898),
        CityPreset("Algiers, DZ", 36.7538, 3.0588),
        CityPreset("Tunis, TN", 36.8065, 10.1815),
        CityPreset("Lagos, NG", 6.5244, 3.3792),
        CityPreset("Nairobi, KE", -1.2921, 36.8219),
        CityPreset("Johannesburg, ZA", -26.2041, 28.0473),
        CityPreset("Cape Town, ZA", -33.9249, 18.4241),

        // ── Europe ────────────────────────────────────────────────────────
        CityPreset("London, UK", 51.5074, -0.1278),
        CityPreset("Birmingham, UK", 52.4862, -1.8904),
        CityPreset("Manchester, UK", 53.4808, -2.2426),
        CityPreset("Bradford, UK", 53.7960, -1.7594),
        CityPreset("Dublin, IE", 53.3498, -6.2603),
        CityPreset("Paris, FR", 48.8566, 2.3522),
        CityPreset("Brussels, BE", 50.8503, 4.3517),
        CityPreset("Amsterdam, NL", 52.3676, 4.9041),
        CityPreset("Berlin, DE", 52.5200, 13.4050),
        CityPreset("Madrid, ES", 40.4168, -3.7038),
        CityPreset("Rome, IT", 41.9028, 12.4964),
        CityPreset("Stockholm, SE", 59.3293, 18.0686),
        CityPreset("Oslo, NO", 59.9139, 10.7522),
        CityPreset("Copenhagen, DK", 55.6761, 12.5683),

        // ── North America ─────────────────────────────────────────────────
        CityPreset("New York, US", 40.7128, -74.0060),
        CityPreset("Chicago, US", 41.8781, -87.6298),
        CityPreset("Detroit, US", 42.3314, -83.0458),
        CityPreset("Houston, US", 29.7604, -95.3698),
        CityPreset("Los Angeles, US", 34.0522, -118.2437),
        CityPreset("Toronto, CA", 43.6532, -79.3832),
        CityPreset("Montreal, CA", 45.5017, -73.5673),
        CityPreset("Vancouver, CA", 49.2827, -123.1207),

        // ── Oceania ───────────────────────────────────────────────────────
        CityPreset("Sydney, AU", -33.8688, 151.2093),
        CityPreset("Melbourne, AU", -37.8136, 144.9631),
        CityPreset("Brisbane, AU", -27.4698, 153.0251),
        CityPreset("Perth, AU", -31.9523, 115.8613),
        CityPreset("Adelaide, AU", -34.9285, 138.6007),
        CityPreset("Canberra, AU", -35.2809, 149.1300),
        CityPreset("Hobart, AU", -42.8821, 147.3272),
        CityPreset("Darwin, AU", -12.4634, 130.8456),
        CityPreset("Auckland, NZ", -36.8485, 174.7633),
    )

    /** Case-insensitive substring match over the label — the list is too long to scroll. */
    fun search(query: String): List<CityPreset> {
        val q = query.trim()
        return if (q.isEmpty()) cities else cities.filter { it.label.contains(q, ignoreCase = true) }
    }
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
