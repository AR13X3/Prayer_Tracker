package com.prayertracker.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A row of public.shares (only owner_id needed). */
@Serializable
data class ShareRow(
    @SerialName("owner_id") val ownerId: String,
)

/** One prayer's streaks, from the prayer_streaks() RPC. */
@Serializable
data class StreakRow(
    val prayer: String,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("best_streak") val bestStreak: Int,
)

/** Result of redeem_invite(). */
@Serializable
data class RedeemRow(
    @SerialName("owner_id") val ownerId: String,
    @SerialName("owner_name") val ownerName: String,
)

/** Result of create_invite() — the created public.invites row (partial). */
@Serializable
data class InviteRow(
    val code: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("max_uses") val maxUses: Int,
    val mutual: Boolean,
)

/** A prayer_logs row within a date range, for streak history / heatmaps. */
@Serializable
data class PrayerLogDateRow(
    @SerialName("prayer_date") val prayerDate: String,
    val prayer: String,
    val status: String,
)
