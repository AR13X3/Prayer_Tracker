import { useToday, type PrayerRow } from './useToday'
import { SoftCard, SectionLabel } from '../components/design/SoftCard'
import { OutlinePill, StatusPill } from '../components/design/Buttons'
import { ProgressRing } from '../components/design/ProgressRing'
import { WeekStrip } from '../components/design/WeekStrip'
import { Menu, MenuItem, MenuDivider } from '../components/design/Menu'
import { PRAYER_STATUSES, statusLabel, type PrayerStatus } from '../domain/prayer'

const TIME_FMT = new Intl.DateTimeFormat(undefined, { hour: 'numeric', minute: '2-digit' })
const DATE_FMT = new Intl.DateTimeFormat(undefined, { weekday: 'long', day: 'numeric', month: 'short' })

export function TodayScreen() {
  const t = useToday()

  if (t.loading && t.rows.length === 0) {
    return <div className="flex min-h-[60vh] items-center justify-center text-muted">Loading…</div>
  }

  if (t.error && t.rows.length === 0) {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 p-6 text-center">
        <p>Couldn't load: {t.error}</p>
        <OutlinePill onClick={() => t.load()}>Retry</OutlinePill>
      </div>
    )
  }

  return (
    <div className="space-y-4 p-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-ink">Assalamu alaikum</h1>
          <p className="text-sm text-muted">{DATE_FMT.format(t.selectedDate)}</p>
        </div>
        <OutlinePill onClick={() => t.load(t.today)}>Today</OutlinePill>
      </div>

      <WeekStrip dates={t.weekDates} selected={t.selectedDate} today={t.today} onSelect={t.select} />

      <SoftCard padding="p-6">
        <div className="flex items-center gap-5">
          <ProgressRing progress={t.fardRows.length ? t.done / t.fardRows.length : 0} size={116} stroke={13}>
            <div className="text-center">
              <div className="text-2xl font-bold">{t.done}</div>
              <div className="text-[11px] text-muted">of {t.fardRows.length}</div>
            </div>
          </ProgressRing>
          <div>
            <SectionLabel text="Today" />
            <p className="mt-0.5 text-lg font-bold">
              {t.done === t.fardRows.length && t.fardRows.length > 0 ? 'All prayers kept' : `${t.done} of ${t.fardRows.length} prayed`}
            </p>
            <p className="text-sm text-muted">{t.locationLabel}</p>
          </div>
        </div>
      </SoftCard>

      {t.error && <p className="text-sm text-danger">{t.error}</p>}

      {t.usingDefaultLocation && (
        <SoftCard padding="p-4">
          <p className="text-sm text-muted">
            Using a default location (Sydney). Set your location in Settings for accurate times.
          </p>
        </SoftCard>
      )}

      <SectionLabel text="Prayers" />
      <div className="space-y-3">
        {t.fardRows.map((row) => (
          <PrayerCard
            key={row.name}
            row={row}
            onStatus={(s) => t.setStatus(row.name, s)}
            onToggleJamaah={() => t.toggleJamaah(row.name)}
            onClear={() => t.clear(row.name)}
          />
        ))}
      </div>

      {t.naflRows.length > 0 && (
        <>
          <SectionLabel text="Optional · not counted in streaks" />
          <div className="space-y-3">
            {t.naflRows.map((row) => (
              <NaflCard key={row.name} row={row} onToggle={() => t.togglePrayed(row.name)} />
            ))}
          </div>
        </>
      )}
    </div>
  )
}

function PrayerCard({
  row,
  onStatus,
  onToggleJamaah,
  onClear,
}: {
  row: PrayerRow
  onStatus: (s: PrayerStatus) => void
  onToggleJamaah: () => void
  onClear: () => void
}) {
  return (
    <SoftCard>
      <div className="flex items-center gap-3">
        <div className="flex-1">
          <div className="font-semibold">{row.label}</div>
          <div className="text-sm text-muted">{TIME_FMT.format(row.time)}</div>
        </div>
        {row.status && <StatusPill text="Jama'ah" selected={row.inJamaah} onClick={onToggleJamaah} />}
        <Menu trigger={(open) => <StatusPill text={statusLabel(row.status)} selected={!!row.status} onClick={open} />}>
          {(close) => (
            <>
              {PRAYER_STATUSES.map((s) => (
                <MenuItem
                  key={s.db}
                  onClick={() => {
                    onStatus(s.db)
                    close()
                  }}
                >
                  {s.label}
                </MenuItem>
              ))}
              {row.status && (
                <>
                  <MenuDivider />
                  <MenuItem
                    onClick={() => {
                      onClear()
                      close()
                    }}
                  >
                    Clear
                  </MenuItem>
                </>
              )}
            </>
          )}
        </Menu>
      </div>
    </SoftCard>
  )
}

function NaflCard({ row, onToggle }: { row: PrayerRow; onToggle: () => void }) {
  return (
    <SoftCard>
      <div className="flex items-center gap-3">
        <div className="flex-1">
          <div className="font-semibold">{row.label}</div>
          <div className="text-sm text-muted">{TIME_FMT.format(row.time)}</div>
        </div>
        <StatusPill text={row.status ? 'Prayed' : 'Log'} selected={!!row.status} onClick={onToggle} />
      </div>
    </SoftCard>
  )
}
