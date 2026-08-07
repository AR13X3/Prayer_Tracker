package com.prayertracker.app.data

import com.prayertracker.app.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class ProfileRepository(
    private val client: SupabaseClient = Supabase.client,
) {
    /** The signed-in user's profile, or null if not authenticated / not found. RLS scopes it. */
    suspend fun getMyProfile(): Profile? {
        val id = client.auth.currentUserOrNull()?.id ?: return null
        return client.from("profiles")
            .select(
                Columns.list(
                    "id", "display_name", "timezone", "latitude", "longitude",
                    "city_label", "calculation_method", "madhab",
                ),
            ) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull()
    }

    /** Updates only the columns the Settings screen owns. RLS restricts this to the caller's row. */
    suspend fun updateProfile(
        displayName: String,
        timezone: String,
        latitude: Double?,
        longitude: Double?,
        cityLabel: String?,
        calculationMethod: String,
        madhab: String,
    ) {
        val id = client.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        client.from("profiles").update({
            set("display_name", displayName)
            set("timezone", timezone)
            set("latitude", latitude)
            set("longitude", longitude)
            set("city_label", cityLabel)
            set("calculation_method", calculationMethod)
            set("madhab", madhab)
        }) {
            filter { eq("id", id) }
        }
    }
}
