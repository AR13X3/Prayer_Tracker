import { useStreaks } from './useStreaks'
import { ScreenHeader } from '../components/design/SoftCard'
import { OutlinePill } from '../components/design/Buttons'
import { StreaksContent } from '../components/StreaksContent'

export function StreaksScreen() {
  const { loading, error, data, reload } = useStreaks()

  return (
    <div className="space-y-4 p-5">
      <ScreenHeader title="Streaks" subtitle="Your consistency" />
      {loading && <div className="py-16 text-center text-muted">Loading…</div>}
      {error && !loading && (
        <div className="flex flex-col items-center gap-3 py-10 text-center">
          <p>Couldn't load streaks: {error}</p>
          <OutlinePill onClick={reload}>Retry</OutlinePill>
        </div>
      )}
      {data && !loading && <StreaksContent data={data} />}
    </div>
  )
}
