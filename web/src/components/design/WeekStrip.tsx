import { toDateKey } from '../../data/prayerLogRepository'

const WEEKDAY_NARROW = ['S', 'M', 'T', 'W', 'T', 'F', 'S']

/**
 * Rolling 7-day window ending today (NOT a calendar Mon-Sun week) — Isha can run past
 * midnight, so whoever logs it after the date rolls over needs "yesterday" to still be one
 * tap away. See the Android app's TodayViewModel.weekOf() for the same fix and rationale.
 */
export function rollingWeek(today: Date): Date[] {
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(today)
    d.setDate(d.getDate() - (6 - i))
    return d
  })
}

export function WeekStrip({
  dates,
  selected,
  today,
  onSelect,
}: {
  dates: Date[]
  selected: Date
  today: Date
  onSelect: (d: Date) => void
}) {
  const selectedKey = toDateKey(selected)
  const todayKey = toDateKey(today)

  return (
    <div className="flex gap-1.5">
      {dates.map((d) => {
        const key = toDateKey(d)
        const isSelected = key === selectedKey
        const isToday = key === todayKey
        return (
          <button
            key={key}
            onClick={() => onSelect(d)}
            className="flex-1 flex flex-col items-center gap-1.5"
          >
            <span className="text-xs text-muted">{WEEKDAY_NARROW[d.getDay()]}</span>
            <span
              className={`flex h-10 w-10 items-center justify-center rounded-full text-sm font-semibold
                transition-colors
                ${isSelected ? 'bg-coral text-white' : isToday ? 'bg-ink text-white' : 'bg-transparent text-ink'}`}
            >
              {d.getDate()}
            </span>
          </button>
        )
      })}
    </div>
  )
}
