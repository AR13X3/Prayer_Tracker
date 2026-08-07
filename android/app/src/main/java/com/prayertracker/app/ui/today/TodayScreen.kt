package com.prayertracker.app.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertracker.app.domain.PrayerStatus
import com.prayertracker.app.ui.design.OutlinePill
import com.prayertracker.app.ui.design.ProgressRing
import com.prayertracker.app.ui.design.SectionLabel
import com.prayertracker.app.ui.design.SoftCard
import com.prayertracker.app.ui.design.StatusPill

private val PrayedStatuses = setOf(PrayerStatus.ON_TIME, PrayerStatus.LATE, PrayerStatus.QADA)

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    vm: TodayViewModel = viewModel(),
) {
    val s by vm.ui.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        when {
            s.loading && s.rows.isEmpty() ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            s.error != null && s.rows.isEmpty() ->
                Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Couldn't load: ${s.error}")
                    OutlinePill("Retry", onClick = { vm.load() })
                }

            else -> {
                val fardRows = s.rows.filter { it.name.isFard }
                val naflRows = s.rows.filterNot { it.name.isFard }
                val done = fardRows.count { it.status in PrayedStatuses }
                val progress = if (fardRows.isEmpty()) 0f else done.toFloat() / fardRows.size

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { Header(dateLabel = s.dateLabel, onToday = { vm.load(s.today) }) }
                    item {
                        if (s.weekDates.isNotEmpty()) {
                            com.prayertracker.app.ui.design.WeekStrip(
                                dates = s.weekDates,
                                selected = s.selectedDate,
                                today = s.today,
                                onSelect = { vm.select(it) },
                            )
                        }
                    }
                    item { HeroCard(done = done, total = fardRows.size, progress = progress, locationLabel = s.locationLabel) }
                    s.error?.let { err -> item { Text(err, color = MaterialTheme.colorScheme.error) } }
                    if (s.usingDefaultLocation) item { LocationBanner() }

                    item { SectionLabel("Prayers") }
                    items(fardRows, key = { it.name.db }) { row ->
                        PrayerCard(
                            row = row,
                            onStatus = { vm.setStatus(row.name, it) },
                            onToggleJamaah = { vm.toggleJamaah(row.name) },
                            onClear = { vm.clear(row.name) },
                        )
                    }
                    if (naflRows.isNotEmpty()) {
                        item { SectionLabel("Optional · not counted in streaks") }
                        items(naflRows, key = { it.name.db }) { row ->
                            NaflCard(row = row, onToggle = { vm.togglePrayed(row.name) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(dateLabel: String, onToday: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Assalamu alaikum", style = MaterialTheme.typography.headlineMedium)
            if (dateLabel.isNotEmpty()) {
                Text(dateLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        OutlinePill("Today", onClick = onToday)
    }
}

@Composable
private fun HeroCard(done: Int, total: Int, progress: Float, locationLabel: String) {
    SoftCard(Modifier.fillMaxWidth(), padding = 24.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress = progress, diameter = 116.dp, stroke = 13.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$done", style = MaterialTheme.typography.headlineMedium)
                    Text("of $total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                SectionLabel("Today")
                Spacer(Modifier.height(4.dp))
                Text(
                    if (done == total && total > 0) "All prayers kept" else "$done of $total prayed",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    locationLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LocationBanner() {
    SoftCard(Modifier.fillMaxWidth(), padding = 16.dp) {
        Text(
            "Using a default location (Sydney). Set your location in Settings for accurate times.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrayerCard(
    row: PrayerRowUi,
    onStatus: (PrayerStatus) -> Unit,
    onToggleJamaah: () -> Unit,
    onClear: () -> Unit,
) {
    SoftCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(row.timeLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (row.status != null) {
                StatusPill("Jama'ah", selected = row.inJamaah, onClick = onToggleJamaah)
                Spacer(Modifier.width(8.dp))
            }
            StatusControl(row = row, onStatus = onStatus, onClear = onClear)
        }
    }
}

@Composable
private fun StatusControl(
    row: PrayerRowUi,
    onStatus: (PrayerStatus) -> Unit,
    onClear: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        StatusPill(
            text = row.status?.label ?: "Log",
            selected = row.status != null,
            onClick = { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PrayerStatus.entries.forEach { st ->
                DropdownMenuItem(text = { Text(st.label) }, onClick = { expanded = false; onStatus(st) })
            }
            if (row.status != null) {
                HorizontalDivider()
                DropdownMenuItem(text = { Text("Clear") }, onClick = { expanded = false; onClear() })
            }
        }
    }
}

@Composable
private fun NaflCard(row: PrayerRowUi, onToggle: () -> Unit) {
    SoftCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(row.timeLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusPill(
                text = if (row.status != null) "Prayed" else "Log",
                selected = row.status != null,
                onClick = onToggle,
            )
        }
    }
}
