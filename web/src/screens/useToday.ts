import { useCallback, useEffect, useRef, useState } from 'react'
import { computePrayerTimes } from '../lib/prayerTimes'
import { clearLog, logsForDate, toDateKey, upsertLog } from '../data/prayerLogRepository'
import { getMyProfile, updateProfile } from '../data/profileRepository'
import { PRAYERS, type PrayerName, type PrayerStatus } from '../domain/prayer'
import { rollingWeek } from '../components/design/WeekStrip'

export interface PrayerRow {
  name: PrayerName
  label: string
  time: Date
  status: PrayerStatus | null
  inJamaah: boolean
}

const SYDNEY = { lat: -33.8688, lng: 151.2093 }
const PRAYED: PrayerStatus[] = ['on_time', 'late', 'qada']

function sameDay(a: Date, b: Date) {
  return toDateKey(a) === toDateKey(b)
}

export function useToday() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [rows, setRows] = useState<PrayerRow[]>([])
  const [locationLabel, setLocationLabel] = useState('')
  const [usingDefaultLocation, setUsingDefaultLocation] = useState(false)
  const [today, setToday] = useState(() => new Date())
  const [selectedDate, setSelectedDate] = useState(() => new Date())
  const selectedRef = useRef(selectedDate)
  selectedRef.current = selectedDate

  const load = useCallback(async (date?: Date) => {
    setLoading(true)
    setError(null)
    try {
      const profile = await getMyProfile()
      if (!profile) throw new Error('Profile not found')

      const now = new Date()
      // Best-effort write-back of the device zone, mirroring the Android app: the client is
      // the source of truth for "what timezone am I in", never the stored column.
      const zone = Intl.DateTimeFormat().resolvedOptions().timeZone
      if (profile.timezone !== zone) {
        updateProfile({
          displayName: profile.display_name,
          timezone: zone,
          latitude: profile.latitude,
          longitude: profile.longitude,
          cityLabel: profile.city_label,
          calculationMethod: profile.calculation_method,
          madhab: profile.madhab,
        }).catch(() => {})
      }

      const week = rollingWeek(now)
      const target = date ?? (week.some((d) => sameDay(d, selectedRef.current)) ? selectedRef.current : now)

      const usingDefault = profile.latitude == null || profile.longitude == null
      const lat = profile.latitude ?? SYDNEY.lat
      const lng = profile.longitude ?? SYDNEY.lng

      const times = computePrayerTimes(lat, lng, target, profile.calculation_method, profile.madhab)
      const logs = await logsForDate(toDateKey(target))
      const byPrayer = new Map(logs.map((l) => [l.prayer, l]))
      const isFriday = target.getDay() === 5

      const nextRows: PrayerRow[] = PRAYERS.map((p) => {
        const log = byPrayer.get(p.name)
        return {
          name: p.name,
          label: p.name === 'dhuhr' && isFriday ? 'Jummah' : p.label,
          time: times[p.name],
          status: (log?.status as PrayerStatus) ?? null,
          inJamaah: log?.in_jamaah ?? false,
        }
      })

      setRows(nextRows)
      setLocationLabel(
        profile.city_label ?? (usingDefault ? 'Default location: Sydney' : `${lat.toFixed(3)}, ${lng.toFixed(3)}`),
      )
      setUsingDefaultLocation(usingDefault)
      setToday(now)
      setSelectedDate(target)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function select(date: Date) {
    if (!sameDay(date, selectedRef.current)) load(date)
  }

  async function setStatus(prayer: PrayerName, status: PrayerStatus) {
    const prev = rows.find((r) => r.name === prayer)
    if (!prev) return
    setRows((rs) => rs.map((r) => (r.name === prayer ? { ...r, status } : r)))
    try {
      await upsertLog(toDateKey(selectedDate), prayer, status, prev.inJamaah)
    } catch (e) {
      setRows((rs) => rs.map((r) => (r.name === prayer ? { ...r, status: prev.status } : r)))
      setError(`Couldn't save: ${e instanceof Error ? e.message : e}`)
    }
  }

  async function toggleJamaah(prayer: PrayerName) {
    const prev = rows.find((r) => r.name === prayer)
    if (!prev?.status) return
    const next = !prev.inJamaah
    setRows((rs) => rs.map((r) => (r.name === prayer ? { ...r, inJamaah: next } : r)))
    try {
      await upsertLog(toDateKey(selectedDate), prayer, prev.status, next)
    } catch (e) {
      setRows((rs) => rs.map((r) => (r.name === prayer ? { ...r, inJamaah: prev.inJamaah } : r)))
      setError(`Couldn't save: ${e instanceof Error ? e.message : e}`)
    }
  }

  function togglePrayed(prayer: PrayerName) {
    const row = rows.find((r) => r.name === prayer)
    if (!row) return
    if (row.status) void clear(prayer)
    else void setStatus(prayer, 'on_time')
  }

  async function clear(prayer: PrayerName) {
    const prev = rows.find((r) => r.name === prayer)
    if (!prev) return
    setRows((rs) => rs.map((r) => (r.name === prayer ? { ...r, status: null, inJamaah: false } : r)))
    try {
      await clearLog(toDateKey(selectedDate), prayer)
    } catch (e) {
      setRows((rs) => rs.map((r) => (r.name === prayer ? prev : r)))
      setError(`Couldn't clear: ${e instanceof Error ? e.message : e}`)
    }
  }

  const fardRows = rows.filter((r) => PRAYERS.find((p) => p.name === r.name)?.isFard)
  const naflRows = rows.filter((r) => !PRAYERS.find((p) => p.name === r.name)?.isFard)
  const done = fardRows.filter((r) => r.status && PRAYED.includes(r.status)).length

  return {
    loading,
    error,
    rows,
    fardRows,
    naflRows,
    done,
    locationLabel,
    usingDefaultLocation,
    today,
    selectedDate,
    weekDates: rollingWeek(today),
    load,
    select,
    setStatus,
    toggleJamaah,
    togglePrayed,
    clear,
  }
}
