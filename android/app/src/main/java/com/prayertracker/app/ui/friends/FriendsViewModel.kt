package com.prayertracker.app.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertracker.app.data.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class FriendUi(val id: String, val name: String, val topStreak: Int)

data class FriendsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val friends: List<FriendUi> = emptyList(),
    val redeemInput: String = "",
    val redeeming: Boolean = false,
    val message: String? = null,
    val creating: Boolean = false,
    val generatedCode: String? = null,
)

class FriendsViewModel : ViewModel() {

    private val repo = SocialRepository()

    private val _ui = MutableStateFlow(FriendsUiState())
    val ui: StateFlow<FriendsUiState> = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val today = LocalDate.now(ZoneId.systemDefault())
                repo.myFriends().map { p ->
                    val top = runCatching { repo.streaks(p.id, today).maxOfOrNull { it.currentStreak } ?: 0 }
                        .getOrDefault(0)
                    FriendUi(p.id, p.displayName, top)
                }
            }.onSuccess { list -> _ui.update { it.copy(loading = false, friends = list) } }
                .onFailure { e -> _ui.update { it.copy(loading = false, error = e.message ?: "Failed to load") } }
        }
    }

    fun onRedeemInput(v: String) = _ui.update { it.copy(redeemInput = v, message = null) }

    fun redeem() {
        val code = _ui.value.redeemInput.trim()
        if (code.isEmpty()) return
        _ui.update { it.copy(redeeming = true, message = null) }
        viewModelScope.launch {
            runCatching { repo.redeem(code) }
                .onSuccess { row ->
                    _ui.update {
                        it.copy(
                            redeeming = false,
                            redeemInput = "",
                            message = if (row != null) "You're now connected with ${row.ownerName}." else "Redeemed.",
                        )
                    }
                    load()
                }
                .onFailure { e ->
                    // The RPC returns one generic message for invalid/expired/used/revoked codes.
                    _ui.update { it.copy(redeeming = false, message = e.message ?: "Invalid or expired code") }
                }
        }
    }

    fun createInvite() {
        _ui.update { it.copy(creating = true, message = null, generatedCode = null) }
        viewModelScope.launch {
            runCatching { repo.createInvite() }
                .onSuccess { inv -> _ui.update { it.copy(creating = false, generatedCode = inv.code) } }
                .onFailure { e -> _ui.update { it.copy(creating = false, message = e.message ?: "Couldn't create invite") } }
        }
    }

    fun removeFriend(id: String) {
        viewModelScope.launch {
            runCatching { repo.removeFriend(id) }
                .onSuccess { load() }
                .onFailure { e -> _ui.update { it.copy(message = e.message ?: "Couldn't remove") } }
        }
    }
}
