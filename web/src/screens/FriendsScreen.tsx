import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Flame } from 'lucide-react'
import { createInvite, myFriends, redeem, removeFriend, streaks } from '../data/socialRepository'
import { toDateKey } from '../data/prayerLogRepository'
import { SoftCard, SectionLabel, ScreenHeader } from '../components/design/SoftCard'
import { PillButton, StatusPill } from '../components/design/Buttons'

interface Friend {
  id: string
  name: string
  topStreak: number
}

export function FriendsScreen() {
  const navigate = useNavigate()
  const [friends, setFriends] = useState<Friend[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [redeemInput, setRedeemInput] = useState('')
  const [redeeming, setRedeeming] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [generatedCode, setGeneratedCode] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const today = toDateKey(new Date())
      const list = await myFriends()
      const withStreaks = await Promise.all(
        list.map(async (p) => {
          const top = await streaks(p.id, today)
            .then((rows) => Math.max(0, ...rows.map((r) => r.current_streak)))
            .catch(() => 0)
          return { id: p.id, name: p.display_name, topStreak: top }
        }),
      )
      setFriends(withStreaks)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  async function onRedeem() {
    if (!redeemInput.trim()) return
    setRedeeming(true)
    setMessage(null)
    try {
      const row = await redeem(redeemInput)
      setMessage(row ? `You're now connected with ${row.owner_name}.` : 'Redeemed.')
      setRedeemInput('')
      load()
    } catch (e) {
      setMessage(e instanceof Error ? e.message : 'Invalid or expired code')
    } finally {
      setRedeeming(false)
    }
  }

  async function onCreateInvite() {
    setCreating(true)
    setMessage(null)
    setGeneratedCode(null)
    try {
      const inv = await createInvite()
      setGeneratedCode(inv.code)
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "Couldn't create invite")
    } finally {
      setCreating(false)
    }
  }

  async function onRemove(id: string) {
    try {
      await removeFriend(id)
      load()
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "Couldn't remove")
    }
  }

  return (
    <div className="space-y-4 p-5">
      <ScreenHeader title="Friends" subtitle="Keep each other accountable" />

      <SoftCard>
        <SectionLabel text="Add a friend" />
        <p className="mt-1 text-sm text-muted">Paste an invite code someone shared with you.</p>
        <div className="mt-3 flex gap-2.5">
          <input
            className="input"
            placeholder="Invite code"
            value={redeemInput}
            onChange={(e) => setRedeemInput(e.target.value)}
          />
          <PillButton onClick={onRedeem} disabled={redeeming || !redeemInput.trim()}>
            {redeeming ? '…' : 'Redeem'}
          </PillButton>
        </div>
      </SoftCard>

      <SoftCard>
        <SectionLabel text="Invite someone" />
        {generatedCode ? (
          <>
            <p className="mt-2 text-center text-3xl font-bold text-coral">{generatedCode}</p>
            <p className="mt-2 text-center text-sm text-muted">Single-use · expires in 14 days</p>
            <div className="mt-3 flex justify-center">
              <StatusPill text="Create another" selected={false} onClick={onCreateInvite} />
            </div>
          </>
        ) : (
          <>
            <p className="mt-1 text-sm text-muted">Create a code and send it to a friend. They paste it to connect.</p>
            <div className="mt-3">
              <PillButton accent fullWidth onClick={onCreateInvite} disabled={creating}>
                {creating ? 'Creating…' : 'Create invite code'}
              </PillButton>
            </div>
          </>
        )}
      </SoftCard>

      {message && <p className="text-coral">{message}</p>}

      <SectionLabel text="Your friends" />
      {error && <p className="text-danger">Couldn't load: {error}</p>}
      {!loading && !error && friends.length === 0 && (
        <p className="text-muted">No friends yet. Redeem a code or share yours.</p>
      )}
      <div className="space-y-3">
        {friends.map((f) => (
          <SoftCard key={f.id}>
            <div className="flex items-center gap-3">
              <div className="flex-1">
                <div className="font-semibold">{f.name}</div>
                <div className="flex items-center gap-1 text-sm text-muted">
                  <Flame size={15} className="text-coral" />
                  {f.topStreak} best current streak
                </div>
              </div>
              <StatusPill text="View" selected={false} onClick={() => navigate(`/friend/${f.id}`)} />
              <button onClick={() => onRemove(f.id)} className="text-sm font-medium text-danger">
                Remove
              </button>
            </div>
          </SoftCard>
        ))}
      </div>
    </div>
  )
}
