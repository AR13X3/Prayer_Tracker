package com.prayertracker.app.qibla

import android.hardware.GeomagneticField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertracker.app.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QiblaUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val hasLocation: Boolean = false,
    val bearing: Float = 0f,        // Qibla bearing from TRUE north
    val declination: Float = 0f,    // magnetic → true north correction for this location
    val locationLabel: String = "",
)

class QiblaViewModel : ViewModel() {

    private val profileRepo = ProfileRepository()

    private val _ui = MutableStateFlow(QiblaUiState())
    val ui: StateFlow<QiblaUiState> = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.value = QiblaUiState(loading = true)
        viewModelScope.launch {
            runCatching { profileRepo.getMyProfile() ?: error("Profile not found") }
                .onSuccess { p ->
                    val lat = p.latitude
                    val lng = p.longitude
                    if (lat == null || lng == null) {
                        _ui.value = QiblaUiState(loading = false, hasLocation = false)
                    } else {
                        val declination = GeomagneticField(
                            lat.toFloat(), lng.toFloat(), 0f, System.currentTimeMillis(),
                        ).declination
                        _ui.value = QiblaUiState(
                            loading = false,
                            hasLocation = true,
                            bearing = QiblaMath.bearing(lat, lng).toFloat(),
                            declination = declination,
                            locationLabel = p.cityLabel ?: "%.3f, %.3f".format(lat, lng),
                        )
                    }
                }
                .onFailure { e -> _ui.value = QiblaUiState(loading = false, error = e.message ?: "Failed to load") }
        }
    }
}
