import { useCallback, useEffect, useState } from 'react'
import { getMyProfile } from '../data/profileRepository'
import { qiblaBearing } from '../lib/prayerTimes'

// iOS Safari gates the orientation sensor behind an explicit permission prompt, only
// callable from a user gesture. No such type exists in lib.dom.d.ts yet.
type DeviceOrientationEventIOS = typeof DeviceOrientationEvent & {
  requestPermission?: () => Promise<'granted' | 'denied'>
}

export function useQibla() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [hasLocation, setHasLocation] = useState(false)
  const [bearing, setBearing] = useState(0)
  const [locationLabel, setLocationLabel] = useState('')

  const [needsPermission, setNeedsPermission] = useState(false)
  const [permissionDenied, setPermissionDenied] = useState(false)
  const [heading, setHeading] = useState<number | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const profile = await getMyProfile()
      if (!profile) throw new Error('Profile not found')
      if (profile.latitude == null || profile.longitude == null) {
        setHasLocation(false)
      } else {
        setHasLocation(true)
        setBearing(qiblaBearing(profile.latitude, profile.longitude))
        setLocationLabel(profile.city_label ?? `${profile.latitude.toFixed(3)}, ${profile.longitude.toFixed(3)}`)
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    const iosApi = (window as unknown as { DeviceOrientationEvent?: DeviceOrientationEventIOS }).DeviceOrientationEvent
    setNeedsPermission(typeof iosApi?.requestPermission === 'function')
  }, [])

  const startCompass = useCallback(() => {
    function onOrientation(e: DeviceOrientationEvent) {
      // iOS: webkitCompassHeading is magnetic heading, already 0-360 clockwise from north —
      // no sign flip needed. Other browsers: alpha counts counter-clockwise, so invert it.
      // Neither path corrects for magnetic declination (true vs magnetic north) — a browser
      // has no practical way to compute that; the error is small enough for this use case.
      const webkitHeading = (e as DeviceOrientationEvent & { webkitCompassHeading?: number }).webkitCompassHeading
      if (typeof webkitHeading === 'number') {
        setHeading(webkitHeading)
      } else if (e.alpha != null) {
        setHeading((360 - e.alpha) % 360)
      }
    }

    const iosApi = (window as unknown as { DeviceOrientationEvent?: DeviceOrientationEventIOS }).DeviceOrientationEvent
    if (typeof iosApi?.requestPermission === 'function') {
      iosApi
        .requestPermission()
        .then((result) => {
          if (result === 'granted') {
            window.addEventListener('deviceorientation', onOrientation)
          } else {
            setPermissionDenied(true)
          }
        })
        .catch(() => setPermissionDenied(true))
    } else {
      window.addEventListener('deviceorientationabsolute', onOrientation as EventListener)
      window.addEventListener('deviceorientation', onOrientation)
    }

    return () => {
      window.removeEventListener('deviceorientation', onOrientation)
      window.removeEventListener('deviceorientationabsolute', onOrientation as EventListener)
    }
  }, [])

  return { loading, error, hasLocation, bearing, locationLabel, needsPermission, permissionDenied, heading, load, startCompass }
}
