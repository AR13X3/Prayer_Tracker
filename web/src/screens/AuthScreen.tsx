import { useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { PillButton } from '../components/design/Buttons'

export function AuthScreen() {
  const { signUp, signIn } = useAuth()
  const [mode, setMode] = useState<'signIn' | 'signUp'>('signIn')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [info, setInfo] = useState<string | null>(null)

  const isSignUp = mode === 'signUp'

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setInfo(null)

    if (!email.includes('@')) return setError('Enter a valid email')
    if (password.length < 6) return setError('Password must be at least 6 characters')
    if (isSignUp && !displayName.trim()) return setError('Enter a display name')

    setSubmitting(true)
    try {
      if (isSignUp) {
        await signUp(email, password, displayName)
        setInfo('Account created. If email confirmation is enabled, confirm then sign in.')
      } else {
        await signIn(email, password)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-dvh items-center justify-center p-7">
      <form onSubmit={submit} className="w-full max-w-sm space-y-3.5">
        <h1 className="text-3xl font-bold text-coral">Prayer Tracker</h1>
        <h2 className="text-xl font-bold text-ink">{isSignUp ? 'Create your account' : 'Welcome back'}</h2>

        {isSignUp && (
          <input
            className="input"
            placeholder="Display name"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
          />
        )}
        <input
          className="input"
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          className="input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {error && <p className="text-sm text-danger">{error}</p>}
        {info && <p className="text-sm text-coral">{info}</p>}

        <PillButton type="submit" fullWidth disabled={submitting}>
          {submitting ? 'Please wait…' : isSignUp ? 'Sign up' : 'Sign in'}
        </PillButton>

        <button
          type="button"
          disabled={submitting}
          onClick={() => {
            setMode(isSignUp ? 'signIn' : 'signUp')
            setError(null)
            setInfo(null)
          }}
          className="w-full text-center text-sm text-muted py-2"
        >
          {isSignUp ? 'Already have an account? Sign in' : 'New here? Create an account'}
        </button>
      </form>
    </div>
  )
}
