package com.prayertracker.app.ui.streaks

import com.prayertracker.app.data.SocialRepository
import com.prayertracker.app.domain.PrayerName
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class PrayerStreakUi(val name: PrayerName, val current: Int, val best: Int)

/** One day in the recent-weeks heatmap. [count] = prayers kept (on_time/late) that day, 0..5. */
data class DayCellUi(val dayLabel: String, val count: Int, val isToday: Boolean)

data class StreaksData(
    val streaks: List<PrayerStreakUi>,
    val heatmap: List<DayCellUi>,
    val rangeLabel: String,
)

/** Loads streaks + a 35-day heatmap for [target] (self or a friend). RLS scopes visibility. */
object StreaksLoader {
    private const val HEATMAP_DAYS = 35
    private val RANGE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")

    suspend fun load(repo: SocialRepository, target: String): StreaksData {
        val today = LocalDate.now(ZoneId.systemDefault())
        val byPrayer = repo.streaks(target, today).associateBy { it.prayer }
        val streaks = PrayerName.fard.map { p ->
            val r = byPrayer[p.db]
            PrayerStreakUi(p, r?.currentStreak ?: 0, r?.bestStreak ?: 0)
        }

        val start = today.minusDays((HEATMAP_DAYS - 1).toLong())
        val fardDb = PrayerName.fard.map { it.db }.toSet()
        val keptByDate = repo.logsBetween(target, start, today)
            .filter { (it.status == "on_time" || it.status == "late") && it.prayer in fardDb }
            .groupBy { it.prayerDate }
            .mapValues { entry -> entry.value.map { it.prayer }.distinct().size }

        val cells = (0 until HEATMAP_DAYS).map { i ->
            val d = start.plusDays(i.toLong())
            DayCellUi(
                dayLabel = d.dayOfMonth.toString(),
                count = keptByDate[d.toString()] ?: 0,
                isToday = d == today,
            )
        }
        return StreaksData(streaks, cells, "${start.format(RANGE_FMT)} – ${today.format(RANGE_FMT)}")
    }
}
