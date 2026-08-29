import { supabase } from '../lib/supabase'
import type { InviteRow, PrayerLogDateRow, Profile, RedeemRow, StreakRow } from './models'

const PROFILE_COLUMNS = 'id, display_name, timezone, latitude, longitude, city_label, calculation_method, madhab'

export async function myUserId(): Promise<string | null> {
  const {
    data: { user },
  } = await supabase.auth.getUser()
  return user?.id ?? null
}

/** People whose logs I can see: owners who shared with me. */
export async function myFriends(): Promise<Profile[]> {
  const me = await myUserId()
  if (!me) return []

  const { data: shares, error: sharesErr } = await supabase.from('shares').select('owner_id').eq('viewer_id', me)
  if (sharesErr) throw sharesErr

  const ownerIds = [...new Set((shares ?? []).map((s) => s.owner_id as string))]
  if (ownerIds.length === 0) return []

  const { data, error } = await supabase.from('profiles').select(PROFILE_COLUMNS).in('id', ownerIds)
  if (error) throw error
  return data as Profile[]
}

export async function getProfile(userId: string): Promise<Profile | null> {
  const { data, error } = await supabase.from('profiles').select(PROFILE_COLUMNS).eq('id', userId).maybeSingle()
  if (error) throw error
  return data as Profile | null
}

export async function streaks(target: string, today: string): Promise<StreakRow[]> {
  const { data, error } = await supabase.rpc('prayer_streaks', { target, today })
  if (error) throw error
  return data as StreakRow[]
}

export async function redeem(code: string): Promise<RedeemRow | null> {
  const { data, error } = await supabase.rpc('redeem_invite', { invite_code: code.trim().toUpperCase() })
  if (error) throw error
  const rows = data as RedeemRow[]
  return rows?.[0] ?? null
}

/** Creates a single-use, 14-day, mutual invite (all SQL defaults). */
export async function createInvite(): Promise<InviteRow> {
  const { data, error } = await supabase.rpc('create_invite')
  if (error) throw error
  return data as InviteRow
}

export async function logsBetween(target: string, from: string, to: string): Promise<PrayerLogDateRow[]> {
  const { data, error } = await supabase
    .from('prayer_logs')
    .select('prayer_date, prayer, status')
    .eq('user_id', target)
    .gte('prayer_date', from)
    .lte('prayer_date', to)
  if (error) throw error
  return data as PrayerLogDateRow[]
}

/** Removes both directions of the friendship (plan §7.3 recommendation). */
export async function removeFriend(ownerId: string): Promise<void> {
  const me = await myUserId()
  if (!me) return
  await supabase.from('shares').delete().eq('owner_id', ownerId).eq('viewer_id', me)
  await supabase.from('shares').delete().eq('owner_id', me).eq('viewer_id', ownerId)
}
