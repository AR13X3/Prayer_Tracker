package com.prayertracker.app.data

import com.prayertracker.app.data.model.InviteRow
import com.prayertracker.app.data.model.PrayerLogDateRow
import com.prayertracker.app.data.model.Profile
import com.prayertracker.app.data.model.RedeemRow
import com.prayertracker.app.data.model.ShareRow
import com.prayertracker.app.data.model.StreakRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate

/**
 * Invites, friendships (shares), and streaks. All server logic lives in the SECURITY DEFINER
 * RPCs (redeem_invite, create_invite) and the RLS-scoped prayer_streaks — the client just calls them.
 */
class SocialRepository(
    private val client: SupabaseClient = Supabase.client,
) {
    private fun uid(): String? = client.auth.currentUserOrNull()?.id

    fun myUserId(): String? = uid()

    private val profileColumns = Columns.list(
        "id", "display_name", "timezone", "latitude", "longitude",
        "city_label", "calculation_method", "madhab",
    )

    /** People whose logs I can see: owners who shared with me. */
    suspend fun myFriends(): List<Profile> {
        val me = uid() ?: return emptyList()
        val ownerIds = client.from("shares")
            .select(Columns.list("owner_id")) { filter { eq("viewer_id", me) } }
            .decodeList<ShareRow>()
            .map { it.ownerId }
            .distinct()
        if (ownerIds.isEmpty()) return emptyList()
        return client.from("profiles")
            .select(profileColumns) { filter { isIn("id", ownerIds) } }
            .decodeList()
    }

    suspend fun profile(userId: String): Profile? =
        client.from("profiles")
            .select(profileColumns) { filter { eq("id", userId) } }
            .decodeSingleOrNull()

    suspend fun streaks(target: String, today: LocalDate): List<StreakRow> =
        client.postgrest.rpc(
            "prayer_streaks",
            buildJsonObject {
                put("target", target)
                put("today", today.toString())
            },
        ).decodeList()

    suspend fun redeem(code: String): RedeemRow? =
        client.postgrest.rpc(
            "redeem_invite",
            buildJsonObject { put("invite_code", code.trim().uppercase()) },
        ).decodeList<RedeemRow>().firstOrNull()

    /** Creates a single-use, 14-day, mutual invite (all SQL defaults). */
    suspend fun createInvite(): InviteRow =
        client.postgrest.rpc("create_invite").decodeAs()

    suspend fun logsBetween(target: String, from: LocalDate, to: LocalDate): List<PrayerLogDateRow> =
        client.from("prayer_logs")
            .select(Columns.list("prayer_date", "prayer", "status")) {
                filter {
                    eq("user_id", target)
                    gte("prayer_date", from.toString())
                    lte("prayer_date", to.toString())
                }
            }
            .decodeList()

    /** Removes both directions of the friendship (plan §7.3 recommendation). */
    suspend fun removeFriend(ownerId: String) {
        val me = uid() ?: return
        client.from("shares").delete {
            filter { eq("owner_id", ownerId); eq("viewer_id", me) }
        }
        client.from("shares").delete {
            filter { eq("owner_id", me); eq("viewer_id", ownerId) }
        }
    }
}
