package com.prayertracker.app.prayer

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.SunnahTimes
import com.batoulapps.adhan2.data.DateComponents
import com.prayertracker.app.domain.PrayerName
import java.time.LocalDate
import kotlin.time.ExperimentalTime

/**
 * Pure, offline prayer-time calculation via adhan2. Returns each prayer's time as a UTC
 * epoch-millis Long; the caller converts to the user's zone for display.
 *
 * The stored [method] string (profiles.calculation_method) is matched to adhan2's
 * CalculationMethod enum by normalizing away case and separators — so "MuslimWorldLeague",
 * "MUSLIM_WORLD_LEAGUE" and "muslim world league" all resolve to the same value.
 */
object PrayerTimesCalculator {

    @OptIn(ExperimentalTime::class)
    fun compute(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        method: String,
        madhab: String,
    ): Map<PrayerName, Long> {
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val params = calcMethod(method).parameters.copy(madhab = madhabOf(madhab))
        val t = PrayerTimes(coordinates, dateComponents, params)
        // Tahajjud window = start of the last third of the night (tonight's, from today's times).
        val tahajjud = SunnahTimes(t).lastThirdOfTheNight.toEpochMilliseconds()
        return linkedMapOf(
            PrayerName.FAJR to t.fajr.toEpochMilliseconds(),
            PrayerName.DHUHR to t.dhuhr.toEpochMilliseconds(),
            PrayerName.ASR to t.asr.toEpochMilliseconds(),
            PrayerName.MAGHRIB to t.maghrib.toEpochMilliseconds(),
            PrayerName.ISHA to t.isha.toEpochMilliseconds(),
            PrayerName.TAHAJJUD to tahajjud,
        )
    }

    private fun calcMethod(stored: String): CalculationMethod {
        val norm = stored.filter(Char::isLetterOrDigit).uppercase()
        return CalculationMethod.entries.firstOrNull {
            it.name.filter(Char::isLetterOrDigit).uppercase() == norm
        } ?: CalculationMethod.MUSLIM_WORLD_LEAGUE
    }

    private fun madhabOf(stored: String): Madhab =
        if (stored.trim().equals("hanafi", ignoreCase = true)) Madhab.HANAFI else Madhab.SHAFI
}
