package com.prayertracker.app.qibla

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** Great-circle initial bearing from a location to the Kaaba — the Qibla direction. */
object QiblaMath {
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262

    /** Bearing in degrees clockwise from true north (0..360). */
    fun bearing(lat: Double, lng: Double): Double {
        val phi = Math.toRadians(lat)
        val phiK = Math.toRadians(KAABA_LAT)
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val y = sin(dLng)
        val x = cos(phi) * tan(phiK) - sin(phi) * cos(dLng)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    private val CARDINALS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    fun cardinal(deg: Double): String = CARDINALS[(((deg + 22.5) % 360.0) / 45.0).toInt() % 8]
}
