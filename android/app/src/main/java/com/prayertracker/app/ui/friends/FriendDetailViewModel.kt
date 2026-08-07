package com.prayertracker.app.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertracker.app.data.SocialRepository
import com.prayertracker.app.ui.streaks.StreaksData
import com.prayertracker.app.ui.streaks.StreaksLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendDetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val name: String = "Friend",
    val data: StreaksData? = null,
)

class FriendDetailViewModel(private val ownerId: String) : ViewModel() {

    private val repo = SocialRepository()

    private val _ui = MutableStateFlow(FriendDetailUiState())
    val ui: StateFlow<FriendDetailUiState> = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.value = FriendDetailUiState(loading = true)
        viewModelScope.launch {
            runCatching {
                val name = repo.profile(ownerId)?.displayName ?: "Friend"
                val data = StreaksLoader.load(repo, ownerId)
                name to data
            }.onSuccess { (name, data) ->
                _ui.value = FriendDetailUiState(loading = false, name = name, data = data)
            }.onFailure { e ->
                _ui.value = FriendDetailUiState(loading = false, error = e.message ?: "Failed to load")
            }
        }
    }
}
