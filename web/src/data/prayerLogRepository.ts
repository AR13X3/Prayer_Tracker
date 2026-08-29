import { supabase } from '../lib/supabase'
import type { PrayerLogRow } from './models'
import type { PrayerName, PrayerStatus } from '../domain/prayer'

/** yyyy-MM-dd in the LOCAL calendar, never derived from a UTC timestamp — see plan §7.1. */
export function toDateKey(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export async function logsForDate(dateKey: string): Promise<PrayerLogRow[]> {
  const {
    data: { user },
  } = await supabase.auth.getUser()
  if (!user) return []

  const { data, error } = await supabase
    .from('prayer_logs')
    .select('prayer, status, in_jamaah')
    .eq('user_id', user.id)
    .eq('prayer_date', dateKey)
  if (error) throw error
  return data as PrayerLogRow[]
}

export async function upsertLog(
  dateKey: string,
  prayer: PrayerName,
  status: PrayerStatus,
  inJamaah: boolean,
): Promise<void> {
  const {
    data: { user },
  } = await supabase.auth.getUser()
  if (!user) throw new Error('Not authenticated')

  const { error } = await supabase
    .from('prayer_logs')
    .upsert(
      { user_id: user.id, prayer_date: dateKey, prayer, status, in_jamaah: inJamaah },
      { onConflict: 'user_id,prayer_date,prayer' },
    )
  if (error) throw error
}

export async function clearLog(dateKey: string, prayer: PrayerName): Promise<void> {
  const {
    data: { user },
  } = await supabase.auth.getUser()
  if (!user) return

  const { error } = await supabase
    .from('prayer_logs')
    .delete()
    .eq('user_id', user.id)
    .eq('prayer_date', dateKey)
    .eq('prayer', prayer)
  if (error) throw error
}
