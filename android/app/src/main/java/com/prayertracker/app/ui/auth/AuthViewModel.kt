package com.prayertracker.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertracker.app.data.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthMode { SignIn, SignUp }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
    val info: String? = null,
)

class AuthViewModel : ViewModel() {

    // Plain property (not a constructor param) so viewModel() can instantiate via the
    // real no-arg constructor. Swap for a ViewModelFactory when DI arrives.
    private val repo = AuthRepository()

    val sessionStatus: StateFlow<SessionStatus> = repo.sessionStatus

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun onEmail(v: String) = _ui.update { it.copy(email = v, error = null) }
    fun onPassword(v: String) = _ui.update { it.copy(password = v, error = null) }
    fun onDisplayName(v: String) = _ui.update { it.copy(displayName = v, error = null) }

    fun toggleMode() = _ui.update {
        it.copy(
            mode = if (it.mode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn,
            error = null,
            info = null,
        )
    }

    fun submit() {
        val s = _ui.value
        val validation = validate(s)
        if (validation != null) {
            _ui.update { it.copy(error = validation) }
            return
        }
        _ui.update { it.copy(submitting = true, error = null, info = null) }
        viewModelScope.launch {
            runCatching {
                if (s.mode == AuthMode.SignUp) {
                    repo.signUp(s.email, s.password, s.displayName)
                } else {
                    repo.signIn(s.email, s.password)
                }
            }.onSuccess {
                // If email confirmation is ON in Supabase, sign-up won't produce a session
                // immediately; surface a hint. On success sessionStatus drives navigation.
                _ui.update {
                    it.copy(
                        submitting = false,
                        info = if (s.mode == AuthMode.SignUp)
                            "Account created. If email confirmation is enabled, confirm then sign in."
                        else null,
                    )
                }
            }.onFailure { e ->
                _ui.update { it.copy(submitting = false, error = e.message ?: "Something went wrong") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            // Clear the local cache while we still know who's signed in, then end the session.
            runCatching { com.prayertracker.app.data.PrayerLogRepository().clearLocal() }
            runCatching { repo.signOut() }
        }
    }

    private fun validate(s: AuthUiState): String? = when {
        s.email.isBlank() || !s.email.contains("@") -> "Enter a valid email"
        s.password.length < 6 -> "Password must be at least 6 characters"
        s.mode == AuthMode.SignUp && s.displayName.isBlank() -> "Enter a display name"
        else -> null
    }
}

private inline fun MutableStateFlow<AuthUiState>.update(block: (AuthUiState) -> AuthUiState) {
    value = block(value)
}
