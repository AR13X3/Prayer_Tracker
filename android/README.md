# Prayer Tracker — Android (Phase 2: skeleton + auth)

Kotlin + Jetpack Compose + Material 3, talking to Supabase via **supabase-kt 3.7.0**.
This phase proves the auth loop end-to-end: sign up → profile row auto-created by the DB
trigger → session persists across restarts → sign out.

## Prerequisites

- Android Studio (recent stable — it ships the SDK + JDK 17).
- Phase 1 applied to your Supabase project (the `../supabase/` migrations).

## First-time setup

1. **Open the project.** In Android Studio: *Open* → select this `android/` folder (not the
   repo root). Let it sync.

   > **Gradle wrapper jar:** this scaffold ships `gradle-wrapper.properties` but not the
   > binary `gradle-wrapper.jar` (it can't be authored as text). Android Studio regenerates
   > it automatically on first sync. If you build from the command line instead and there's
   > no `gradlew` yet, run `gradle wrapper` once (needs a system Gradle), then use `./gradlew`.

2. **Add your Supabase keys.** Copy the example and fill it in:
   ```bash
   cp local.properties.example local.properties
   ```
   Set `SUPABASE_URL` and `SUPABASE_ANON_KEY` from *Supabase dashboard → Project Settings →
   API*. Use the **anon / publishable** key — never the `service_role` key (it bypasses RLS).
   Re-sync Gradle after editing.

3. **Run** on an emulator or device (API 26+). You should land on the sign-in screen.

## What works in this phase

- **Sign up** (email, password, display name). The display name is sent as auth user
  metadata → the `handle_new_user` trigger writes the `profiles` row. No client-side profile
  insert needed.
- **Sign in / sign out.**
- **Session persistence:** supabase-kt restores the session on launch, so a returning user
  goes straight to Home. `MainActivity` switches screens purely on `auth.sessionStatus`.
- **Home** is a placeholder that greets the user — Phase 3 (Today screen, prayer times,
  logging) builds on it.

### Email confirmation note
If *Authentication → Providers → Email → Confirm email* is **ON** in your Supabase project,
sign-up won't create a session immediately — the user must confirm via the emailed link,
then sign in. The UI shows a hint for this. For easy testing you can turn confirmation off.

## Layout

```
app/src/main/java/com/prayertracker/app/
  PrayerTrackerApp.kt        Application; warms the Supabase client
  MainActivity.kt            session-status → screen routing
  data/
    Supabase.kt              the single SupabaseClient (Auth + Postgrest)
    AuthRepository.kt        signUp / signIn / signOut / sessionStatus
  ui/
    theme/Theme.kt           Material 3 theme
    auth/AuthViewModel.kt    form state + validation + submit
    auth/AuthScreen.kt       sign-in / sign-up UI
    home/HomeScreen.kt       placeholder Today screen
```

## If Gradle sync complains about versions

All versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml). The values are
known-good as of Aug 2026 but your installed Android Studio may want newer `agp`/`composeBom`
— accept its suggestion or bump there. If any `io.github.jan.supabase.*` import is unresolved,
it's almost always a version/package rename between supabase-kt releases; check the
[3.7.0 release notes](https://github.com/supabase-community/supabase-kt/releases).
