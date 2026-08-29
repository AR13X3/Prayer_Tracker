package com.prayertracker.app.ui.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.prayertracker.app.BuildConfig
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertracker.app.domain.CalculationMethods
import com.prayertracker.app.domain.PrayerName
import com.prayertracker.app.domain.Presets
import com.prayertracker.app.reminders.Notifications
import com.prayertracker.app.reminders.PrayerAlarmScheduler
import com.prayertracker.app.reminders.ReminderBootstrap
import com.prayertracker.app.reminders.ReminderPrefs
import com.prayertracker.app.ui.design.OutlinePill
import com.prayertracker.app.ui.design.PillButton
import com.prayertracker.app.ui.design.ScreenHeader
import com.prayertracker.app.ui.design.SectionLabel
import com.prayertracker.app.ui.design.SoftCard
import com.prayertracker.app.ui.design.StatusPill

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = viewModel(),
) {
    val s by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(s.saved) { if (s.saved) ReminderBootstrap.sync(context) }

    if (s.loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader("Settings")

        SoftCard(Modifier.fillMaxWidth()) {
            SectionLabel("Profile")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.displayName,
                onValueChange = vm::onDisplayName,
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SoftCard(Modifier.fillMaxWidth()) {
            SectionLabel("Location")
            Spacer(Modifier.height(10.dp))
            val currentCity = if (s.useCustom) "Custom" else (s.cityLabel ?: "Choose a city")
            CityDropdown(currentLabel = currentCity, vm = vm)
            if (s.useCustom) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = s.customLat,
                        onValueChange = vm::onCustomLat,
                        label = { Text("Latitude") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = s.customLng,
                        onValueChange = vm::onCustomLng,
                        label = { Text("Longitude") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Times shown in ${s.timezone} — from your phone's clock, so it updates itself when you travel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SoftCard(Modifier.fillMaxWidth()) {
            SectionLabel("Calculation method")
            Spacer(Modifier.height(10.dp))
            MethodDropdown(currentStored = s.calculationMethod, vm = vm)
        }

        SoftCard(Modifier.fillMaxWidth()) {
            SectionLabel("Asr madhab")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill("Shafi", selected = s.madhab == "shafi", onClick = { vm.onMadhab("shafi") })
                StatusPill("Hanafi", selected = s.madhab == "hanafi", onClick = { vm.onMadhab("hanafi") })
            }
        }

        s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (s.saved) Text("Saved. Return to Today and tap Today to refresh.", color = MaterialTheme.colorScheme.primary)

        PillButton(
            text = if (s.saving) "Saving…" else "Save changes",
            onClick = vm::save,
            enabled = !s.saving,
            modifier = Modifier.fillMaxWidth(),
        )

        SoftCard(Modifier.fillMaxWidth()) { ReminderSettings() }

        SoftCard(Modifier.fillMaxWidth()) { AboutSection() }

        OutlinePill(
            text = "Sign out",
            onClick = {
                PrayerAlarmScheduler.cancelAll(context)
                ReminderPrefs(context).userId = null
                onSignOut()
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ReminderSettings() {
    val context = LocalContext.current
    val prefs = remember { ReminderPrefs(context) }
    var master by remember { mutableStateOf(prefs.masterEnabled) }
    val perPrayer = remember {
        mutableStateMapOf<PrayerName, Boolean>().apply {
            PrayerName.entries.forEach { put(it, prefs.isEnabled(it)) }
        }
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    SectionLabel("Reminders")
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Prayer-time reminders", Modifier.weight(1f))
        Switch(
            checked = master,
            onCheckedChange = { on ->
                master = on
                prefs.masterEnabled = on
                if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifications.hasPermission(context)) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                PrayerAlarmScheduler.reschedule(context)
            },
        )
    }

    if (master) {
        PrayerName.entries.forEach { p ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(p.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = perPrayer[p] == true,
                    onCheckedChange = { on ->
                        perPrayer[p] = on
                        prefs.setEnabled(p, on)
                        PrayerAlarmScheduler.reschedule(context)
                    },
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(AlarmManager::class.java)
            if (am?.canScheduleExactAlarms() == false) {
                Spacer(Modifier.height(8.dp))
                OutlinePill(
                    text = "Allow exact alarms",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData(Uri.fromParts("package", context.packageName, null)),
                        )
                    },
                )
            }
        }
        if (!Notifications.hasPermission(context)) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Notifications are off for this app — reminders won't show until you enable them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Version tracker: shows what build is installed, and a link to the GitHub Releases page —
 * the same place Obtainium watches for updates, so this doubles as a manual "check now".
 */
@Composable
private fun AboutSection() {
    val context = LocalContext.current
    SectionLabel("About")
    Spacer(Modifier.height(10.dp))
    Text(
        "Prayer Tracker · version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "Updates deliver automatically via Obtainium once it's set up — see the README, or check manually below.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(12.dp))
    OutlinePill(
        text = "View releases on GitHub",
        onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${BuildConfig.GITHUB_REPO_URL}/releases")))
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CityDropdown(currentLabel: String, vm: SettingsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val matches = remember(query) { Presets.search(query) }

    fun dismiss() {
        expanded = false
        query = ""
    }

    Box(Modifier.fillMaxWidth()) {
        OutlinePill(currentLabel, onClick = { expanded = true }, modifier = Modifier.fillMaxWidth())
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = ::dismiss,
            modifier = Modifier.heightIn(max = 420.dp),
        ) {
            // The preset list is long enough that scrolling it is worse than typing.
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search") },
                singleLine = true,
                modifier = Modifier.padding(horizontal = 12.dp).width(260.dp),
            )
            Spacer(Modifier.height(4.dp))

            if (matches.isEmpty()) {
                Text(
                    "No city matches \"$query\". Use Custom below to enter coordinates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).width(260.dp),
                )
            }
            matches.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.label) },
                    onClick = { dismiss(); vm.onSelectCity(c.label, c.latitude, c.longitude) },
                )
            }
            DropdownMenuItem(
                text = { Text("Custom (enter coordinates)") },
                onClick = { dismiss(); vm.onChooseCustom() },
            )
        }
    }
}

@Composable
private fun MethodDropdown(currentStored: String, vm: SettingsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinePill(CalculationMethods.labelFor(currentStored), onClick = { expanded = true }, modifier = Modifier.fillMaxWidth())
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CalculationMethods.options.forEach { (label, stored) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { expanded = false; vm.onMethod(stored) })
            }
        }
    }
}
