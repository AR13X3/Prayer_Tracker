import { Flame } from 'lucide-react'
import { SoftCard, SectionLabel } from './design/SoftCard'
import type { StreaksData } from '../screens/useStreaks'

export function StreaksContent({ data }: { data: StreaksData }) {
  return (
    <div className="space-y-4">
      <SectionLabel text="Per-prayer streaks" />
      <div className="space-y-3">
        {data.streaks.map((s) => (
          <SoftCard key={s.name}>
            <div className="flex items-center">
              <div className="flex-1 font-semibold">{s.label}</div>
              <div className="flex items-center gap-1 text-coral">
                <Flame size={18} />
                <span className="text-lg font-bold">{s.current}</span>
              </div>
              <div className="ml-5 text-right">
                <div className="text-lg font-bold">{s.best}</div>
                <div className="text-[11px] text-muted">best</div>
              </div>
            </div>
          </SoftCard>
        ))}
      </div>

      <SectionLabel text={`Recent weeks · ${data.rangeLabel}`} />
      <SoftCard>
        <Heatmap cells={data.heatmap} />
        <p className="mt-3 text-sm text-muted">Each square is a day; darker = more of the 5 prayers kept.</p>
      </SoftCard>
    </div>
  )
}

function Heatmap({ cells }: { cells: StreaksData['heatmap'] }) {
  const weeks: (typeof cells)[] = []
  for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7))

  return (
    <div className="flex flex-col gap-1.5">
      {weeks.map((week, wi) => (
        <div key={wi} className="flex gap-1.5">
          {week.map((cell, ci) => (
            <div
              key={ci}
              className={`flex h-8 w-8 items-center justify-center rounded-lg text-[11px] text-muted
                ${cell.isToday ? 'ring-2 ring-ink' : ''}`}
              style={{
                backgroundColor:
                  cell.count === 0 ? 'var(--color-track)' : `color-mix(in oklab, var(--color-coral) ${25 + 75 * (cell.count / 5)}%, transparent)`,
              }}
            >
              {cell.dayLabel}
            </div>
          ))}
        </div>
      ))}
    </div>
  )
}
