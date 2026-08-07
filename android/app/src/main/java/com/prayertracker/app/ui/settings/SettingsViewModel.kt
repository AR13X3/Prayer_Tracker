package com.prayertracker.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertracker.app.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val displayName: String = "",
    val timezone: String = "Australia/Sydney",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val cityLabel: String? = null,
    val calculationMethod: String = "MuslimWorldLeague",
    val madhab: String = "shafi",
    // free-text coordinate fields, used only when "Custom" is chosen
    val customLat: String = "",
    val customLng: String = "",
    val useCustom: Boolean = false,
)

class SettingsViewModel : ViewModel() {

    private val profileRepo = ProfileRepository()

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { profileRepo.getMyProfile() ?: error("Profile not found") }
                .onSuccess { p ->
                    _ui.value = SettingsUiState(
                        loading = false,
                        displayName = p.displayName,
                        timezone = p.timezone,
                        latitude = p.latitude,
                        longitude = p.longitude,
                        cityLabel = p.cityLabel,
                        calculationMethod = p.calculationMethod,
                        madhab = p.madhab,
                        customLat = p.latitude?.toString() ?: "",
                        customLng = p.longitude?.toString() ?: "",
                    )
                }
                .onFailure { e -> _ui.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun onDisplayName(v: String) = _ui.update { it.copy(displayName = v, saved = false) }
    fun onMethod(stored: String) = _ui.update { it.copy(calculationMethod = stored, saved = false) }
    fun onMadhab(v: String) = _ui.update { it.copy(madhab = v, saved = false) }
    fun onCustomLat(v: String) = _ui.update { it.copy(customLat = v, saved = false) }
    fun onCustomLng(v: String) = _ui.update { it.copy(customLng = v, saved = false) }

    fun onSelectCity(label: String, lat: Double, lng: Double, timezone: String) = _ui.update {
        it.copy(
            useCustom = false,
            cityLabel = label,
            latitude = lat,
            longitude = lng,
            timezone = timezone,
            customLat = lat.toString(),
            customLng = lng.toString(),
            saved = false,
        )
    }

    fun onChooseCustom() = _ui.update { it.copy(useCustom = true, saved = false) }

    fun save() {
        val s = _ui.value
        val lat: Double?
        val lng: Double?
        val cityLabel: String?
        if (s.useCustom) {
            lat = s.customLat.trim().toDoubleOrNull()
            lng = s.customLng.trim().toDoubleOrNull()
            if (lat == null || lng == null || lat !in -90.0..90.0 || lng !in -180.0..180.0) {
                _ui.update { it.copy(error = "Enter valid coordinates (lat -90..90, lng -180..180)") }
                return
            }
            cityLabel = "Custom (%.3f, %.3f)".format(lat, lng)
        } else {
            lat = s.latitude
            lng = s.longitude
            cityLabel = s.cityLabel
        }
        if (s.displayName.isBlank()) {
            _ui.update { it.copy(error = "Display name can't be empty") }
            return
        }

        _ui.update { it.copy(saving = true, error = null, saved = false) }
        viewModelScope.launch {
            runCatching {
                profileRepo.updateProfile(
                    displayName = s.displayName.trim(),
                    timezone = s.timezone,
                    latitude = lat,
                    longitude = lng,
                    cityLabel = cityLabel,
                    calculationMethod = s.calculationMethod,
                    madhab = s.madhab,
                )
            }.onSuccess {
                _ui.update {
                    it.copy(saving = false, saved = true, latitude = lat, longitude = lng, cityLabel = cityLabel)
                }
            }.onFailure { e -> _ui.update { it.copy(saving = false, error = e.message ?: "Save failed") } }
        }
    }
}
