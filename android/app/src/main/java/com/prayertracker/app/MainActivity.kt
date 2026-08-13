package com.prayertracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prayertracker.app.ui.MainScreen
import com.prayertracker.app.ui.auth.AuthScreen
import com.prayertracker.app.ui.auth.AuthViewModel
import com.prayertracker.app.ui.design.BrandLoadingScreen
import com.prayertracker.app.ui.theme.PrayerTrackerTheme
import io.github.jan.supabase.auth.status.SessionStatus

class MainActivity : ComponentActivity() {

    // Activity-scoped, so the composables below (via viewModel()) share this instance —
    // which is what lets the splash condition read the same session status the UI does.
    private val authVm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Hold the system splash while supabase-kt restores the saved session, so a
        // returning user never sees a flash of the sign-in screen. Session restore is
        // local storage, so this is short; if it ever isn't, BrandLoadingScreen (the same
        // artwork, in Compose) takes over rather than freezing on the splash.
        splash.setKeepOnScreenCondition { authVm.sessionStatus.value is SessionStatus.Initializing }

        enableEdgeToEdge()
        setContent {
            PrayerTrackerTheme {
                Root(vm = authVm)
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
private fun Root(vm: AuthViewModel) {
    val status by vm.sessionStatus.collectAsStateWithLifecycle()

    when (status) {
        is SessionStatus.Authenticated -> MainScreen(onSignOut = vm::signOut)

        // NotAuthenticated, or a refresh failure (token expired offline) -> sign-in.
        is SessionStatus.NotAuthenticated,
        is SessionStatus.RefreshFailure ->
            Scaffold(Modifier.fillMaxSize()) { pad ->
                Box(Modifier.fillMaxSize().padding(pad)) { AuthScreen(vm = vm) }
            }

        // Initializing: restoring a saved session from storage. Normally covered by the
        // system splash; this is the seamless continuation if it takes longer.
        is SessionStatus.Initializing -> BrandLoadingScreen()
    }
}
