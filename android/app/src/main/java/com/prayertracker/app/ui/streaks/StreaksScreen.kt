package com.prayertracker.app.ui.streaks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import com.prayertracker.app.ui.design.LucideIcons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertracker.app.ui.design.OutlinePill
import com.prayertracker.app.ui.design.ScreenHeader
import com.prayertracker.app.ui.design.SectionLabel
import com.prayertracker.app.ui.design.SoftCard

@Composable
fun StreaksScreen(modifier: Modifier = Modifier) {
    val vm: StreaksViewModel = viewModel { StreaksViewModel(null) }
    val s by vm.ui.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        when {
            s.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            s.error != null -> Column(
                Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Couldn't load streaks: ${s.error}")
                OutlinePill("Retry", onClick = vm::load)
            }
            s.data != null -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ScreenHeader("Streaks", subtitle = "Your consistency")
                StreaksContent(s.data!!)
            }
        }
    }
}

/** Reusable streaks view — Streaks tab and Friend detail. */
@Composable
fun StreaksContent(data: StreaksData, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionLabel("Per-prayer streaks")
        data.streaks.forEach { st ->
            SoftCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(st.name.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    StatBlock(value = "${st.current}", label = "current", accent = true, icon = LucideIcons.Flame)
                    Spacer(Modifier.width(20.dp))
                    StatBlock(value = "${st.best}", label = "best", accent = false)
                }
            }
        }

        SectionLabel("Recent weeks · ${data.rangeLabel}")
        SoftCard(Modifier.fillMaxWidth()) {
            Heatmap(data.heatmap)
            Spacer(Modifier.size(12.dp))
            Text(
                "Each square is a day; darker = more of the 5 prayers kept.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String, accent: Boolean, icon: ImageVector? = null) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Heatmap(cells: List<DayCellUi>) {
    val base = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val todayBorder = MaterialTheme.colorScheme.onSurface

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                week.forEach { cell ->
                    val cellShape = RoundedCornerShape(9.dp)
                    val color = if (cell.count == 0) empty else base.copy(alpha = 0.25f + 0.75f * (cell.count / 5f))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(cellShape)
                            .background(color)
                            .then(if (cell.isToday) Modifier.border(2.dp, todayBorder, cellShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            cell.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
