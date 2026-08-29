/** A manual location preset (no browser geolocation permission needed for v1). */
export interface CityPreset {
  label: string
  latitude: number
  longitude: number
}

/** Ported 1:1 from the Android app's Presets.kt, grouped by region. */
export const CITIES: CityPreset[] = [
  // South Asia
  { label: 'Dhaka, BD', latitude: 23.8103, longitude: 90.4125 },
  { label: 'Sylhet, BD', latitude: 24.8949, longitude: 91.8687 },
  { label: 'Chattogram, BD', latitude: 22.3569, longitude: 91.7832 },
  { label: 'Khulna, BD', latitude: 22.8456, longitude: 89.5403 },
  { label: 'Rajshahi, BD', latitude: 24.3745, longitude: 88.6042 },
  { label: 'Karachi, PK', latitude: 24.8607, longitude: 67.0011 },
  { label: 'Lahore, PK', latitude: 31.5204, longitude: 74.3587 },
  { label: 'Islamabad, PK', latitude: 33.6844, longitude: 73.0479 },
  { label: 'Peshawar, PK', latitude: 34.0151, longitude: 71.5249 },
  { label: 'Delhi, IN', latitude: 28.6139, longitude: 77.209 },
  { label: 'Mumbai, IN', latitude: 19.076, longitude: 72.8777 },
  { label: 'Kolkata, IN', latitude: 22.5726, longitude: 88.3639 },
  { label: 'Hyderabad, IN', latitude: 17.385, longitude: 78.4867 },
  { label: 'Bengaluru, IN', latitude: 12.9716, longitude: 77.5946 },
  { label: 'Chennai, IN', latitude: 13.0827, longitude: 80.2707 },
  { label: 'Colombo, LK', latitude: 6.9271, longitude: 79.8612 },
  { label: 'Kathmandu, NP', latitude: 27.7172, longitude: 85.324 },
  { label: 'Kabul, AF', latitude: 34.5553, longitude: 69.2075 },

  // Middle East
  { label: 'Mecca, SA', latitude: 21.4225, longitude: 39.8262 },
  { label: 'Medina, SA', latitude: 24.4686, longitude: 39.6142 },
  { label: 'Riyadh, SA', latitude: 24.7136, longitude: 46.6753 },
  { label: 'Jeddah, SA', latitude: 21.4858, longitude: 39.1925 },
  { label: 'Dubai, AE', latitude: 25.2048, longitude: 55.2708 },
  { label: 'Abu Dhabi, AE', latitude: 24.4539, longitude: 54.3773 },
  { label: 'Doha, QA', latitude: 25.2854, longitude: 51.531 },
  { label: 'Kuwait City, KW', latitude: 29.3759, longitude: 47.9774 },
  { label: 'Manama, BH', latitude: 26.2285, longitude: 50.586 },
  { label: 'Muscat, OM', latitude: 23.588, longitude: 58.3829 },
  { label: 'Amman, JO', latitude: 31.9454, longitude: 35.9284 },
  { label: 'Jerusalem, PS', latitude: 31.7683, longitude: 35.2137 },
  { label: 'Baghdad, IQ', latitude: 33.3152, longitude: 44.3661 },
  { label: 'Tehran, IR', latitude: 35.6892, longitude: 51.389 },
  { label: 'Istanbul, TR', latitude: 41.0082, longitude: 28.9784 },
  { label: 'Ankara, TR', latitude: 39.9334, longitude: 32.8597 },

  // Southeast & East Asia
  { label: 'Kuala Lumpur, MY', latitude: 3.139, longitude: 101.6869 },
  { label: 'Singapore, SG', latitude: 1.3521, longitude: 103.8198 },
  { label: 'Jakarta, ID', latitude: -6.2088, longitude: 106.8456 },
  { label: 'Bandung, ID', latitude: -6.9175, longitude: 107.6191 },
  { label: 'Manila, PH', latitude: 14.5995, longitude: 120.9842 },
  { label: 'Bangkok, TH', latitude: 13.7563, longitude: 100.5018 },
  { label: 'Tashkent, UZ', latitude: 41.2995, longitude: 69.2401 },

  // Africa
  { label: 'Cairo, EG', latitude: 30.0444, longitude: 31.2357 },
  { label: 'Khartoum, SD', latitude: 15.5007, longitude: 32.5599 },
  { label: 'Casablanca, MA', latitude: 33.5731, longitude: -7.5898 },
  { label: 'Algiers, DZ', latitude: 36.7538, longitude: 3.0588 },
  { label: 'Tunis, TN', latitude: 36.8065, longitude: 10.1815 },
  { label: 'Lagos, NG', latitude: 6.5244, longitude: 3.3792 },
  { label: 'Nairobi, KE', latitude: -1.2921, longitude: 36.8219 },
  { label: 'Johannesburg, ZA', latitude: -26.2041, longitude: 28.0473 },
  { label: 'Cape Town, ZA', latitude: -33.9249, longitude: 18.4241 },

  // Europe
  { label: 'London, UK', latitude: 51.5074, longitude: -0.1278 },
  { label: 'Birmingham, UK', latitude: 52.4862, longitude: -1.8904 },
  { label: 'Manchester, UK', latitude: 53.4808, longitude: -2.2426 },
  { label: 'Bradford, UK', latitude: 53.796, longitude: -1.7594 },
  { label: 'Dublin, IE', latitude: 53.3498, longitude: -6.2603 },
  { label: 'Paris, FR', latitude: 48.8566, longitude: 2.3522 },
  { label: 'Brussels, BE', latitude: 50.8503, longitude: 4.3517 },
  { label: 'Amsterdam, NL', latitude: 52.3676, longitude: 4.9041 },
  { label: 'Berlin, DE', latitude: 52.52, longitude: 13.405 },
  { label: 'Madrid, ES', latitude: 40.4168, longitude: -3.7038 },
  { label: 'Rome, IT', latitude: 41.9028, longitude: 12.4964 },
  { label: 'Stockholm, SE', latitude: 59.3293, longitude: 18.0686 },
  { label: 'Oslo, NO', latitude: 59.9139, longitude: 10.7522 },
  { label: 'Copenhagen, DK', latitude: 55.6761, longitude: 12.5683 },

  // North America
  { label: 'New York, US', latitude: 40.7128, longitude: -74.006 },
  { label: 'Chicago, US', latitude: 41.8781, longitude: -87.6298 },
  { label: 'Detroit, US', latitude: 42.3314, longitude: -83.0458 },
  { label: 'Houston, US', latitude: 29.7604, longitude: -95.3698 },
  { label: 'Los Angeles, US', latitude: 34.0522, longitude: -118.2437 },
  { label: 'Toronto, CA', latitude: 43.6532, longitude: -79.3832 },
  { label: 'Montreal, CA', latitude: 45.5017, longitude: -73.5673 },
  { label: 'Vancouver, CA', latitude: 49.2827, longitude: -123.1207 },

  // Oceania
  { label: 'Sydney, AU', latitude: -33.8688, longitude: 151.2093 },
  { label: 'Melbourne, AU', latitude: -37.8136, longitude: 144.9631 },
  { label: 'Brisbane, AU', latitude: -27.4698, longitude: 153.0251 },
  { label: 'Perth, AU', latitude: -31.9523, longitude: 115.8613 },
  { label: 'Adelaide, AU', latitude: -34.9285, longitude: 138.6007 },
  { label: 'Canberra, AU', latitude: -35.2809, longitude: 149.13 },
  { label: 'Hobart, AU', latitude: -42.8821, longitude: 147.3272 },
  { label: 'Darwin, AU', latitude: -12.4634, longitude: 130.8456 },
  { label: 'Auckland, NZ', latitude: -36.8485, longitude: 174.7633 },
]

export function searchCities(query: string): CityPreset[] {
  const q = query.trim().toLowerCase()
  if (!q) return CITIES
  return CITIES.filter((c) => c.label.toLowerCase().includes(q))
}

/** adhan-js's CalculationMethod function names match these exactly — see prayerTimes.ts. */
export const CALCULATION_METHODS: { label: string; stored: string }[] = [
  { label: 'Muslim World League', stored: 'MuslimWorldLeague' },
  { label: 'Egyptian', stored: 'Egyptian' },
  { label: 'Karachi', stored: 'Karachi' },
  { label: 'Umm al-Qura (Makkah)', stored: 'UmmAlQura' },
  { label: 'Dubai', stored: 'Dubai' },
  { label: 'Qatar', stored: 'Qatar' },
  { label: 'Kuwait', stored: 'Kuwait' },
  { label: 'Moonsighting Committee', stored: 'MoonsightingCommittee' },
  { label: 'Singapore', stored: 'Singapore' },
  { label: 'North America (ISNA)', stored: 'NorthAmerica' },
]

export function methodLabel(stored: string): string {
  return (
    CALCULATION_METHODS.find((m) => m.stored.toLowerCase() === stored.toLowerCase())?.label ??
    'Muslim World League'
  )
}
