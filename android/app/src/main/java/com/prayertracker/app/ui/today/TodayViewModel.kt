package com.prayertracker.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertracker.app.data.PrayerLogRepository
import com.prayertracker.app.data.ProfileRepository
import com.prayertracker.app.domain.PrayerName
import com.prayertracker.app.domain.PrayerStatus
import com.prayertracker.app.prayer.PrayerTimesCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class PrayerRowUi(
    val name: PrayerName,
    val label: String,        // usually name.label; "Jummah" for Dhuhr on Fridays
    val timeLabel: String,
    val status: PrayerStatus?,
    val inJamaah: Boolean,
)

data class TodayUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val dateLabel: String = "",
    val locationLabel: String = "",
    val usingDefaultLocation: Boolean = false,
    val rows: List<PrayerRowUi> = emptyList(),
    val weekDates: List<LocalDate> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
)

class TodayViewModel : ViewModel() {

    private val profileRepo = ProfileRepository()
    private val logRepo = PrayerLogRepository()

    private var zone: ZoneId = ZoneId.systemDefault()
    private var selectedDate: LocalDate = LocalDate.now()

    private val _ui = MutableStateFlow(TodayUiState())
    val ui: StateFlow<TodayUiState> = _ui.asStateFlow()

    /** Fard prayers that count as "prayed" for the completion ring. */
    private val prayed = setOf(PrayerStatus.ON_TIME, PrayerStatus.LATE, PrayerStatus.QADA)

    init { load() }

    /** Reloads for [date], or the currently selected date when null. */
    fun load(date: LocalDate? = null) {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val profile = profileRepo.getMyProfile() ?: error("Profile not found")
                zone = runCatching { ZoneId.of(profile.timezone) }.getOrDefault(ZoneId.systemDefault())
                val today = LocalDate.now(zone)
                selectedDate = date ?: selectedDate.coerceIntoWeekOf(today)

                val usingDefault = profile.latitude == null || profile.longitude == null
                val lat = profile.latitude ?: SYDNEY_LAT
                val lng = profile.longitude ?: SYDNEY_LNG

                val times = PrayerTimesCalculator.compute(lat, lng, selectedDate, profile.calculationMethod, profile.madhab)
                val logs = logRepo.logsForDate(selectedDate).associateBy { it.prayer }
                val isFriday = selectedDate.dayOfWeek == DayOfWeek.FRIDAY

                val rows = PrayerName.entries.map { p ->
                    val log = logs[p.db]
                    PrayerRowUi(
                        name = p,
                        label = if (p == PrayerName.DHUHR && isFriday) "Jummah" else p.label,
                        timeLabel = formatTime(times.getValue(p)),
                        status = log?.let { PrayerStatus.fromDb(it.status) },
                        inJamaah = log?.inJamaah ?: false,
                    )
                }
                TodayUiState(
                    loading = false,
                    dateLabel = selectedDate.format(DATE_FMT),
                    locationLabel = profile.cityLabel
                        ?: if (usingDefault) "Default location: Sydney"
                        else "%.3f, %.3f".format(lat, lng),
                    usingDefaultLocation = usingDefault,
                    rows = rows,
                    weekDates = weekOf(today),
                    selectedDate = selectedDate,
                    today = today,
                )
            }.onSuccess { _ui.value = it }
                .onFailure { e -> _ui.update { it.copy(loading = false, error = e.message ?: "Failed to load") } }
        }
    }

    fun select(date: LocalDate) {
        if (date != selectedDate) load(date)
    }

    fun setStatus(prayer: PrayerName, status: PrayerStatus) {
        val prev = rowOf(prayer) ?: return
        updateRow(prayer) { it.copy(status = status) }
        viewModelScope.launch {
            runCatching { logRepo.upsert(selectedDate, prayer, status, prev.inJamaah) }
                .onFailure { e ->
                    updateRow(prayer) { it.copy(status = prev.status) }
                    _ui.update { it.copy(error = "Couldn't save: ${e.message}") }
                }
        }
    }

    fun toggleJamaah(prayer: PrayerName) {
        val prev = rowOf(prayer) ?: return
        val status = prev.status ?: return
        val next = !prev.inJamaah
        updateRow(prayer) { it.copy(inJamaah = next) }
        viewModelScope.launch {
            runCatching { logRepo.upsert(selectedDate, prayer, status, next) }
                .onFailure { e ->
                    updateRow(prayer) { it.copy(inJamaah = prev.inJamaah) }
                    _ui.update { it.copy(error = "Couldn't save: ${e.message}") }
                }
        }
    }

    /** Tahajjud / nafl toggle: prayed = status on_time, unprayed = cleared. */
    fun togglePrayed(prayer: PrayerName) {
        val row = rowOf(prayer) ?: return
        if (row.status != null) clear(prayer) else setStatus(prayer, PrayerStatus.ON_TIME)
    }

    fun clear(prayer: PrayerName) {
        val prev = rowOf(prayer) ?: return
        updateRow(prayer) { it.copy(status = null, inJamaah = false) }
        viewModelScope.launch {
            runCatching { logRepo.clear(selectedDate, prayer) }
                .onFailure { e ->
                    updateRow(prayer) { prev }
                    _ui.update { it.copy(error = "Couldn't clear: ${e.message}") }
                }
        }
    }

    fun fardProgress(): Float {
        val rows = _ui.value.rows.filter { it.name.isFard }
        if (rows.isEmpty()) return 0f
        return rows.count { it.status in prayed }.toFloat() / rows.size
    }

    private fun rowOf(p: PrayerName) = _ui.value.rows.firstOrNull { it.name == p }

    private fun updateRow(p: PrayerName, f: (PrayerRowUi) -> PrayerRowUi) {
        _ui.update { st -> st.copy(rows = st.rows.map { if (it.name == p) f(it) else it }) }
    }

    private fun formatTime(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(TIME_FMT)

    private fun weekOf(today: LocalDate): List<LocalDate> {
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        return (0..6).map { monday.plusDays(it.toLong()) }
    }

    /** Keep the selected date inside the currently displayed week. */
    private fun LocalDate.coerceIntoWeekOf(today: LocalDate): LocalDate {
        val week = weekOf(today)
        return if (this in week) this else today
    }

    private companion object {
        const val SYDNEY_LAT = -33.8688
        const val SYDNEY_LNG = 151.2093
        val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM")
        val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    }
}
