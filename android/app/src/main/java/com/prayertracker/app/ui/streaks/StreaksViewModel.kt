package com.prayertracker.app.ui.streaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertracker.app.data.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StreaksUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val data: StreaksData? = null,
)

/**
 * Streaks for a target user. [target] null = the signed-in user (My streaks tab);
 * a non-null id = a friend (Friend detail). RLS returns all-zero for anyone not visible.
 */
class StreaksViewModel(
    private val target: String? = null,
) : ViewModel() {

    private val repo = SocialRepository()

    private val _ui = MutableStateFlow(StreaksUiState())
    val ui: StateFlow<StreaksUiState> = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val id = target ?: repo.myUserId()
            if (id == null) {
                _ui.value = StreaksUiState(loading = false, error = "Not signed in")
                return@launch
            }
            runCatching { StreaksLoader.load(repo, id) }
                .onSuccess { _ui.value = StreaksUiState(loading = false, data = it) }
                .onFailure { e -> _ui.value = StreaksUiState(loading = false, error = e.message ?: "Failed to load") }
        }
    }
}
