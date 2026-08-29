import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { getProfile } from '../data/socialRepository'
import { loadStreaks, type StreaksData } from './useStreaks'
import { StreaksContent } from '../components/StreaksContent'

export function FriendDetailScreen() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [name, setName] = useState('Friend')
  const [data, setData] = useState<StreaksData | null>(null)

  useEffect(() => {
    if (!id) return
    let cancelled = false
    setLoading(true)
    Promise.all([getProfile(id), loadStreaks(id)])
      .then(([profile, streaks]) => {
        if (cancelled) return
        setName(profile?.display_name ?? 'Friend')
        setData(streaks)
      })
      .catch((e) => !cancelled && setError(e instanceof Error ? e.message : 'Failed to load'))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [id])

  return (
    <div className="space-y-4 p-5">
      <div className="flex items-center gap-3.5">
        <button
          onClick={() => navigate(-1)}
          className="flex h-11 w-11 items-center justify-center rounded-full border-[1.5px] border-track"
        >
          <ArrowLeft size={20} />
        </button>
        <h1 className="text-2xl font-bold">{loading ? '…' : name}</h1>
      </div>
      {loading && <div className="py-16 text-center text-muted">Loading…</div>}
      {error && <p className="text-danger">Couldn't load: {error}</p>}
      {data && !loading && <StreaksContent data={data} />}
    </div>
  )
}
