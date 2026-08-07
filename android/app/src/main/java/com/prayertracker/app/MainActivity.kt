package com.prayertracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertracker.app.ui.MainScreen
import com.prayertracker.app.ui.auth.AuthScreen
import com.prayertracker.app.ui.auth.AuthViewModel
import com.prayertracker.app.ui.theme.PrayerTrackerTheme
import io.github.jan.supabase.auth.status.SessionStatus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrayerTrackerTheme {
                Root()
            }
        }
    }
}

/**
 * Single source of navigation truth: the auth session status decides the screen.
 * Because supabase-kt restores the session from storage on startup, a returning user
 * lands straight on Today without re-authenticating. TodayScreen brings its own
 * Scaffold/app bar; the auth and loading states get a bare Scaffold for window insets.
 */
@Composable
private fun Root(vm: AuthViewModel = viewModel()) {
    val status by vm.sessionStatus.collectAsStateWithLifecycle()

    when (status) {
        is SessionStatus.Authenticated -> MainScreen(onSignOut = vm::signOut)

        // NotAuthenticated, or a refresh failure (token expired offline) -> sign-in.
        is SessionStatus.NotAuthenticated,
        is SessionStatus.RefreshFailure ->
            Scaffold(Modifier.fillMaxSize()) { pad ->
                Box(Modifier.fillMaxSize().padding(pad)) { AuthScreen(vm = vm) }
            }

        // Initializing: restoring a saved session from storage. Brief spinner.
        is SessionStatus.Initializing ->
            Scaffold(Modifier.fillMaxSize()) { pad ->
                Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
    }
}
