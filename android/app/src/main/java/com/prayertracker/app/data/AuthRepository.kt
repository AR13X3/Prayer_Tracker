package com.prayertracker.app.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Thin wrapper over supabase-kt Auth. The rest of the app observes [sessionStatus] and never
 * touches the client directly.
 */
class AuthRepository(
    private val client: io.github.jan.supabase.SupabaseClient = Supabase.client,
) {
    /** Emits Initializing → Authenticated / NotAuthenticated / RefreshFailure. */
    val sessionStatus: StateFlow<SessionStatus> get() = client.auth.sessionStatus

    /**
     * Sign up with email + password. The display name is written to the auth user's
     * metadata, which the Postgres `handle_new_user` trigger reads to create the profile row.
     */
    suspend fun signUp(email: String, password: String, displayName: String) {
        client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = buildJsonObject {
                put("display_name", displayName.trim())
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}
