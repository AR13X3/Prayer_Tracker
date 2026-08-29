import { useEffect, useState } from 'react'
import { useQibla } from './useQibla'
import { SoftCard, SectionLabel, ScreenHeader } from '../components/design/SoftCard'
import { OutlinePill, PillButton } from '../components/design/Buttons'

export function QiblaScreen() {
  const q = useQibla()
  const [started, setStarted] = useState(false)

  useEffect(() => {
    if (!started) return
    return q.startCompass()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [started])

  return (
    <div className="space-y-4 p-5">
      <ScreenHeader title="Qibla" subtitle="Direction of prayer" />

      {q.loading && <div className="py-16 text-center text-muted">Loading…</div>}

      {q.error && !q.loading && (
        <SoftCard>
          <p>Couldn't load: {q.error}</p>
          <div className="mt-2.5">
            <OutlinePill onClick={q.load}>Retry</OutlinePill>
          </div>
        </SoftCard>
      )}

      {!q.loading && !q.error && !q.hasLocation && (
        <SoftCard>
          <SectionLabel text="No location set" />
          <p className="mt-2 text-sm text-muted">Set your location in Settings to find the Qibla direction.</p>
        </SoftCard>
      )}

      {!q.loading && !q.error && q.hasLocation && (
        <>
          {!started ? (
            <SoftCard>
              <p className="text-sm text-muted">
                This compass uses your phone's orientation sensor. Your browser will ask permission the first time.
              </p>
              <div className="mt-3">
                <PillButton
                  fullWidth
                  onClick={() => {
                    setStarted(true)
                  }}
                >
                  Enable compass
                </PillButton>
              </div>
            </SoftCard>
          ) : q.permissionDenied ? (
            <SoftCard>
              <p className="text-sm text-danger">
                Compass access was denied. Check your browser's site settings to allow motion & orientation access,
                then reload.
              </p>
            </SoftCard>
          ) : q.heading == null ? (
            <SoftCard>
              <p className="text-sm text-muted">Waiting for compass data… move your phone gently.</p>
            </SoftCard>
          ) : (
            <QiblaCompass bearing={q.bearing} heading={q.heading} locationLabel={q.locationLabel} />
          )}
        </>
      )}
    </div>
  )
}

function QiblaCompass({ bearing, heading, locationLabel }: { bearing: number; heading: number; locationLabel: string }) {
  const needle = bearing - heading
  const normalized = (((needle % 360) + 540) % 360) - 180
  const aligned = Math.abs(normalized) < 6

  return (
    <>
      <div className="flex justify-center py-4">
        <svg width={280} height={280} viewBox="0 0 280 280">
          <circle cx={140} cy={140} r={124} fill="none" stroke="var(--color-track)" strokeWidth={3} />
          {/* Fixed top marker — where the phone points. */}
          <path d="M 140 22 L 131 40 L 149 40 Z" fill="var(--color-ink)" />
          <g transform={`rotate(${needle} 140 140)`}>
            <path d="M 140 40 L 120 130 L 160 130 Z" fill={aligned ? '#2e9e6b' : 'var(--color-coral)'} />
            <line x1={140} y1={140} x2={140} y2={200} stroke="var(--color-track)" strokeWidth={4} />
          </g>
          <circle cx={140} cy={140} r={7} fill="var(--color-ink)" />
        </svg>
      </div>

      <SoftCard>
        <SectionLabel text="Details" />
        <Row label="Qibla bearing" value={`${Math.round(bearing)}°`} />
        <Row label="Your heading" value={`${Math.round(((heading % 360) + 360) % 360)}°`} />
        <Row label="Location" value={locationLabel} />
        <p className={`mt-2.5 text-lg font-semibold ${aligned ? 'text-coral' : ''}`}>
          {aligned ? "You're facing the Qibla." : normalized > 0 ? 'Turn right toward the arrow.' : 'Turn left toward the arrow.'}
        </p>
      </SoftCard>

      <p className="text-sm text-muted">
        Hold the phone flat. If the needle drifts, move it in a figure-8 to recalibrate the compass.
      </p>
    </>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between py-1">
      <span className="text-sm text-muted">{label}</span>
      <span className="font-semibold">{value}</span>
    </div>
  )
}
