/**
 * Loggable prayers. `db` matches the Postgres `prayer_name` enum. `isFard` marks the five
 * obligatory prayers that count toward streaks; Tahajjud is voluntary and never does.
 *
 * Jummah is not a value here — it's the Friday replacement for Dhuhr (same slot), shown with
 * a "Jummah" label but stored and streak-counted as Dhuhr. See computeRows() in prayerTimes.ts.
 */
export const PRAYERS = [
  { name: 'fajr', label: 'Fajr', isFard: true },
  { name: 'dhuhr', label: 'Dhuhr', isFard: true },
  { name: 'asr', label: 'Asr', isFard: true },
  { name: 'maghrib', label: 'Maghrib', isFard: true },
  { name: 'isha', label: 'Isha', isFard: true },
  { name: 'tahajjud', label: 'Tahajjud', isFard: false },
] as const

export type PrayerName = (typeof PRAYERS)[number]['name']

export const FARD_PRAYERS = PRAYERS.filter((p) => p.isFard).map((p) => p.name)

export function prayerLabel(name: PrayerName): string {
  return PRAYERS.find((p) => p.name === name)?.label ?? name
}

export function isFard(name: PrayerName): boolean {
  return PRAYERS.find((p) => p.name === name)?.isFard ?? false
}

export const PRAYER_STATUSES = [
  { db: 'on_time', label: 'On time' },
  { db: 'late', label: 'Late' },
  { db: 'qada', label: 'Qada' },
  { db: 'missed', label: 'Missed' },
] as const

export type PrayerStatus = (typeof PRAYER_STATUSES)[number]['db']

export function statusLabel(status: PrayerStatus | null): string {
  return PRAYER_STATUSES.find((s) => s.db === status)?.label ?? 'Log'
}
