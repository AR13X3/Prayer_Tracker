package com.prayertracker.app.data

import com.prayertracker.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * The single Supabase client for the app.
 *
 * - [Auth] persists the session automatically on Android (auto-loaded from storage on init),
 *   so the user stays signed in across app restarts with no extra wiring.
 * - [Postgrest] is the PostgREST query interface; every request is filtered by the RLS
 *   policies defined in the `supabase/` migrations — the app has no authorization logic of
 *   its own.
 *
 * URL + anon key come from BuildConfig, injected from local.properties at build time.
 * The anon key is safe to ship; RLS is what protects the data.
 */
object Supabase {
    val client: SupabaseClient by lazy {
        require(BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            "SUPABASE_URL / SUPABASE_ANON_KEY are missing. Copy local.properties.example to " +
                "local.properties and fill them in, then re-sync Gradle."
        }
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
