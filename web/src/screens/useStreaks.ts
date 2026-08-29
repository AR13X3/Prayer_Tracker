import { useCallback, useEffect, useState } from 'react'
import { streaks as fetchStreaks, logsBetween, myUserId } from '../data/socialRepository'
import { toDateKey } from '../data/prayerLogRepository'
import { FARD_PRAYERS, PRAYERS, type PrayerName } from '../domain/prayer'

export interface PrayerStreak {
  name: PrayerName
  label: string
  current: number
  best: number
}

export interface DayCell {
  dayLabel: string
  count: number
  isToday: boolean
}

export interface StreaksData {
  streaks: PrayerStreak[]
  heatmap: DayCell[]
  rangeLabel: string
}

const HEATMAP_DAYS = 35
const RANGE_FMT = new Intl.DateTimeFormat(undefined, { day: 'numeric', month: 'short' })

function addDays(d: Date, n: number) {
  const r = new Date(d)
  r.setDate(r.getDate() + n)
  return r
}

export async function loadStreaks(target: string): Promise<StreaksData> {
  const today = new Date()
  const rows = await fetchStreaks(target, toDateKey(today))
  const byPrayer = new Map(rows.map((r) => [r.prayer, r]))

  const streaks: PrayerStreak[] = FARD_PRAYERS.map((name) => {
    const r = byPrayer.get(name)
    const label = PRAYERS.find((p) => p.name === name)!.label
    return { name, label, current: r?.current_streak ?? 0, best: r?.best_streak ?? 0 }
  })

  const start = addDays(today, -(HEATMAP_DAYS - 1))
  const logs = await logsBetween(target, toDateKey(start), toDateKey(today))
  const fardSet = new Set<string>(FARD_PRAYERS)
  const keptByDate = new Map<string, Set<string>>()
  for (const l of logs) {
    if ((l.status === 'on_time' || l.status === 'late') && fardSet.has(l.prayer)) {
      const set = keptByDate.get(l.prayer_date) ?? new Set<string>()
      set.add(l.prayer)
      keptByDate.set(l.prayer_date, set)
    }
  }

  const heatmap: DayCell[] = Array.from({ length: HEATMAP_DAYS }, (_, i) => {
    const d = addDays(start, i)
    const key = toDateKey(d)
    return { dayLabel: String(d.getDate()), count: keptByDate.get(key)?.size ?? 0, isToday: key === toDateKey(today) }
  })

  return { streaks, heatmap, rangeLabel: `${RANGE_FMT.format(start)} – ${RANGE_FMT.format(today)}` }
}

export function useStreaks(target?: string) {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [data, setData] = useState<StreaksData | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const id = target ?? (await myUserId())
      if (!id) throw new Error('Not signed in')
      setData(await loadStreaks(id))
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load')
    } finally {
      setLoading(false)
    }
  }, [target])

  useEffect(() => {
    load()
  }, [load])

  return { loading, error, data, reload: load }
}
