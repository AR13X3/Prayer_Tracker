export interface Profile {
  id: string
  display_name: string
  timezone: string
  latitude: number | null
  longitude: number | null
  city_label: string | null
  calculation_method: string
  madhab: string
}

export interface PrayerLogRow {
  prayer: string
  status: string
  in_jamaah: boolean
}

export interface StreakRow {
  prayer: string
  current_streak: number
  best_streak: number
}

export interface RedeemRow {
  owner_id: string
  owner_name: string
}

export interface InviteRow {
  code: string
  expires_at: string
  max_uses: number
  mutual: boolean
}

export interface PrayerLogDateRow {
  prayer_date: string
  prayer: string
  status: string
}
