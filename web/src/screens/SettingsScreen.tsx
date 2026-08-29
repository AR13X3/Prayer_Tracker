import { useEffect, useState } from 'react'
import { getMyProfile, updateProfile } from '../data/profileRepository'
import { useAuth } from '../contexts/AuthContext'
import { SoftCard, SectionLabel, ScreenHeader } from '../components/design/SoftCard'
import { OutlinePill, PillButton, StatusPill } from '../components/design/Buttons'
import { Menu, MenuItem } from '../components/design/Menu'
import { CALCULATION_METHODS, methodLabel, searchCities, type CityPreset } from '../domain/presets'

export function SettingsScreen() {
  const { signOut } = useAuth()
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)

  const [displayName, setDisplayName] = useState('')
  const [cityLabel, setCityLabel] = useState<string | null>(null)
  const [lat, setLat] = useState<number | null>(null)
  const [lng, setLng] = useState<number | null>(null)
  const [calcMethod, setCalcMethod] = useState('MuslimWorldLeague')
  const [madhab, setMadhab] = useState('shafi')
  const [citySearch, setCitySearch] = useState('')

  useEffect(() => {
    getMyProfile()
      .then((p) => {
        if (!p) throw new Error('Profile not found')
        setDisplayName(p.display_name)
        setCityLabel(p.city_label)
        setLat(p.latitude)
        setLng(p.longitude)
        setCalcMethod(p.calculation_method)
        setMadhab(p.madhab)
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load'))
      .finally(() => setLoading(false))
  }, [])

  function pickCity(c: CityPreset) {
    setCityLabel(c.label)
    setLat(c.latitude)
    setLng(c.longitude)
    setSaved(false)
  }

  async function save() {
    if (!displayName.trim()) return setError("Display name can't be empty")
    setSaving(true)
    setError(null)
    setSaved(false)
    try {
      await updateProfile({
        displayName: displayName.trim(),
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
        latitude: lat,
        longitude: lng,
        cityLabel,
        calculationMethod: calcMethod,
        madhab,
      })
      setSaved(true)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="py-16 text-center text-muted">Loading…</div>

  return (
    <div className="space-y-4 p-5 pb-8">
      <ScreenHeader title="Settings" />

      <SoftCard>
        <SectionLabel text="Profile" />
        <input
          className="input mt-2.5"
          value={displayName}
          onChange={(e) => {
            setDisplayName(e.target.value)
            setSaved(false)
          }}
          placeholder="Display name"
        />
      </SoftCard>

      <SoftCard>
        <SectionLabel text="Location" />
        <div className="mt-2.5">
          <Menu
            align="left"
            trigger={(open) => (
              <OutlinePill fullWidth onClick={open}>
                {cityLabel ?? 'Choose a city'}
              </OutlinePill>
            )}
          >
            {(close) => (
              <div className="max-h-72 overflow-y-auto">
                <div className="sticky top-0 bg-card p-2">
                  <input
                    autoFocus
                    className="input"
                    placeholder="Search cities…"
                    value={citySearch}
                    onChange={(e) => setCitySearch(e.target.value)}
                  />
                </div>
                {searchCities(citySearch)
                  .slice(0, 50)
                  .map((c) => (
                    <MenuItem
                      key={c.label}
                      onClick={() => {
                        pickCity(c)
                        close()
                      }}
                    >
                      {c.label}
                    </MenuItem>
                  ))}
              </div>
            )}
          </Menu>
        </div>
        <p className="mt-2.5 text-sm text-muted">
          Times shown in your device's timezone — it updates itself when you travel.
        </p>
      </SoftCard>

      <SoftCard>
        <SectionLabel text="Calculation method" />
        <div className="mt-2.5">
          <Menu
            align="left"
            trigger={(open) => (
              <OutlinePill fullWidth onClick={open}>
                {methodLabel(calcMethod)}
              </OutlinePill>
            )}
          >
            {(close) => (
              <>
                {CALCULATION_METHODS.map((m) => (
                  <MenuItem
                    key={m.stored}
                    onClick={() => {
                      setCalcMethod(m.stored)
                      setSaved(false)
                      close()
                    }}
                  >
                    {m.label}
                  </MenuItem>
                ))}
              </>
            )}
          </Menu>
        </div>
      </SoftCard>

      <SoftCard>
        <SectionLabel text="Asr madhab" />
        <div className="mt-2.5 flex gap-2.5">
          <StatusPill
            text="Shafi"
            selected={madhab === 'shafi'}
            onClick={() => {
              setMadhab('shafi')
              setSaved(false)
            }}
          />
          <StatusPill
            text="Hanafi"
            selected={madhab === 'hanafi'}
            onClick={() => {
              setMadhab('hanafi')
              setSaved(false)
            }}
          />
        </div>
      </SoftCard>

      {error && <p className="text-danger">{error}</p>}
      {saved && <p className="text-coral">Saved. Return to Today to refresh.</p>}

      <PillButton fullWidth onClick={save} disabled={saving}>
        {saving ? 'Saving…' : 'Save changes'}
      </PillButton>

      <OutlinePill fullWidth onClick={() => signOut()}>
        Sign out
      </OutlinePill>
    </div>
  )
}
