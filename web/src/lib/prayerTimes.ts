import { CalculationMethod, Coordinates, Madhab, PrayerTimes, Qibla, SunnahTimes } from 'adhan'
import type { PrayerName } from '../domain/prayer'

export type PrayerTimesMap = Record<PrayerName, Date>

/**
 * adhan-js's CalculationMethod export names match profiles.calculation_method exactly
 * (e.g. "MuslimWorldLeague"), unlike the Kotlin port which needed fuzzy matching — direct
 * lookup here. Falls back to Muslim World League for an unrecognized/empty stored value.
 */
function methodParams(stored: string) {
  const fn = (CalculationMethod as Record<string, (() => ReturnType<typeof CalculationMethod.MuslimWorldLeague>) | undefined>)[
    stored
  ]
  return (fn ?? CalculationMethod.MuslimWorldLeague)()
}

/**
 * Computes all six loggable prayer times for [date] at [lat]/[lng]. Tahajjud is the start of
 * the last third of the night (adhan-js's SunnahTimes), matching the Android app exactly.
 */
export function computePrayerTimes(
  lat: number,
  lng: number,
  date: Date,
  calculationMethod: string,
  madhab: string,
): PrayerTimesMap {
  const coordinates = new Coordinates(lat, lng)
  const params = methodParams(calculationMethod)
  params.madhab = madhab === 'hanafi' ? Madhab.Hanafi : Madhab.Shafi

  const times = new PrayerTimes(coordinates, date, params)
  const sunnah = new SunnahTimes(times)

  return {
    fajr: times.fajr,
    dhuhr: times.dhuhr,
    asr: times.asr,
    maghrib: times.maghrib,
    isha: times.isha,
    tahajjud: sunnah.lastThirdOfTheNight,
  }
}

/** Bearing to the Kaaba in degrees clockwise from true north. */
export function qiblaBearing(lat: number, lng: number): number {
  return Qibla(new Coordinates(lat, lng))
}
