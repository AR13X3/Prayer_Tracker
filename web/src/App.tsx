import { NavLink, Route, Routes, useLocation } from 'react-router-dom'
import { Sun, Flame, Compass, Users, Settings2 } from 'lucide-react'
import { useAuth } from './contexts/AuthContext'
import { AuthScreen } from './screens/AuthScreen'
import { TodayScreen } from './screens/TodayScreen'
import { StreaksScreen } from './screens/StreaksScreen'
import { QiblaScreen } from './screens/QiblaScreen'
import { FriendsScreen } from './screens/FriendsScreen'
import { FriendDetailScreen } from './screens/FriendDetailScreen'
import { SettingsScreen } from './screens/SettingsScreen'

const TABS = [
  { to: '/', label: 'Today', Icon: Sun },
  { to: '/streaks', label: 'Streaks', Icon: Flame },
  { to: '/qibla', label: 'Qibla', Icon: Compass },
  { to: '/friends', label: 'Friends', Icon: Users },
  { to: '/settings', label: 'Settings', Icon: Settings2 },
]

export default function App() {
  const { session, loading } = useAuth()

  if (loading) {
    return <div className="flex min-h-dvh items-center justify-center text-muted">Loading…</div>
  }

  if (!session) return <AuthScreen />

  return (
    <div className="min-h-dvh pb-24">
      <Routes>
        <Route path="/" element={<TodayScreen />} />
        <Route path="/streaks" element={<StreaksScreen />} />
        <Route path="/qibla" element={<QiblaScreen />} />
        <Route path="/friends" element={<FriendsScreen />} />
        <Route path="/friend/:id" element={<FriendDetailScreen />} />
        <Route path="/settings" element={<SettingsScreen />} />
      </Routes>
      <BottomNav />
    </div>
  )
}

function BottomNav() {
  const location = useLocation()
  // Friend detail is a sub-page of Friends; keep the Friends tab lit while there, but hide
  // the whole bar isn't necessary since it's still useful nav — matches the Android shell.
  const isFriendDetail = location.pathname.startsWith('/friend/')

  return (
    <nav className="fixed inset-x-0 bottom-0 flex justify-center px-5 py-3.5">
      <div className="flex gap-1 rounded-full bg-card p-2 shadow-[0_8px_30px_rgba(0,0,0,0.14)]">
        {TABS.map(({ to, label, Icon }) => {
          const active = isFriendDetail ? to === '/friends' : location.pathname === to
          return (
            <NavLink
              key={to}
              to={to}
              className={`flex h-12 w-12 items-center justify-center rounded-full transition-colors
                ${active ? 'bg-ink text-white' : 'text-muted'}`}
              aria-label={label}
            >
              <Icon size={22} />
            </NavLink>
          )
        })}
      </div>
    </nav>
  )
}
