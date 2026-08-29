import { supabase } from '../lib/supabase'
import type { Profile } from './models'

const COLUMNS = 'id, display_name, timezone, latitude, longitude, city_label, calculation_method, madhab'

export async function getMyProfile(): Promise<Profile | null> {
  const {
    data: { user },
  } = await supabase.auth.getUser()
  if (!user) return null

  const { data, error } = await supabase.from('profiles').select(COLUMNS).eq('id', user.id).maybeSingle()
  if (error) throw error
  return data as Profile | null
}

export async function updateProfile(patch: {
  displayName: string
  timezone: string
  latitude: number | null
  longitude: number | null
  cityLabel: string | null
  calculationMethod: string
  madhab: string
}): Promise<void> {
  const {
    data: { user },
  } = await supabase.auth.getUser()
  if (!user) throw new Error('Not authenticated')

  const { error } = await supabase
    .from('profiles')
    .update({
      display_name: patch.displayName,
      timezone: patch.timezone,
      latitude: patch.latitude,
      longitude: patch.longitude,
      city_label: patch.cityLabel,
      calculation_method: patch.calculationMethod,
      madhab: patch.madhab,
    })
    .eq('id', user.id)
  if (error) throw error
}
