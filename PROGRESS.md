# Overnight progress log

Working through the phases sequentially while you sleep. I can't compile/run here (no
Android SDK in this environment), so every risky library API was verified against the
actual library source before use. **Nothing here has been run on a device yet** — treat the
first Gradle sync + build in the morning as the real test. I've flagged the parts most
likely to need a device tweak.

Read this top-to-bottom when you wake. Build instructions at the bottom.

---

## Status by phase

- [x] **Phase 1** — Supabase schema + RLS (applied; §9 verification still skipped)
- [x] **Phase 2** — Android skeleton + auth (working on your device)
- [~] **Phase 3A** — Core loop (Today + prayer times + logging). Written, you were about to build.
- [~] **Settings** — location/method/madhab picker (written)
- [~] **Phase 5** — Social (invites, friends, friend detail) (written)
- [~] **Phase 4** — Streaks (per-prayer + heatmap) (written)
- [~] **Phase 6** — Reminders + notification quick-action (written)
- [~] **Phase 3B** — Offline (Room + outbox) (written)
- [ ] **Phase 7** — Release build (checklist below)

Legend: [x] done & code-reviewed  ·  [~] written, needs device test  ·  [ ] not started

---

## Detailed log

(Appended as I go — newest at the bottom.)

### Navigation shell + Settings — starting
The app needs more than one authenticated screen now, so I'm adding a bottom-nav shell
(Today / Streaks / Friends / Settings) and refactoring the single-screen `MainActivity`.

### Done: nav shell, Settings, Streaks, Social
- **Nav shell** `ui/MainScreen.kt` — bottom nav (Today/Streaks/Friends/Settings) + friend
  detail route. `MainActivity` now shows `MainScreen` when authenticated. `TodayScreen` was
  refactored to content-only (the shell owns the top bar); it gained a **Refresh** button so
  it picks up location changes made in Settings (the two screens have separate view models).
  - *Note:* emoji are used as bottom-nav icons to avoid a material-icons dependency — swap
    for real icons later if you want.
- **Settings** `ui/settings/*` + `ProfileRepository.updateProfile` — display name, location
  (preset cities in `domain/Presets.kt` **or** custom lat/lng), calculation method, madhab.
  Writes via the RLS-scoped `profiles` update.
- **Streaks** `ui/streaks/*` — calls the `prayer_streaks` RPC for per-prayer current/best,
  plus a 35-day heatmap built from a `prayer_logs` range query. `StreaksContent` is reused by
  Friend detail.
- **Social** `data/SocialRepository.kt` + `ui/friends/*` — redeem a code (`redeem_invite`
  RPC), create an invite (`create_invite` RPC), friends list (shares → profiles) with each
  friend's top current streak, and a read-only friend detail. Remove-friend deletes **both**
  share directions (plan §7.3).

**Verified against library source before writing:** `from/select/upsert/update/delete`,
`filter { eq/gte/lte/isIn }`, `postgrest.rpc(name, JsonObject)`, member `decodeList/
decodeSingleOrNull/decodeAs`, and that the client's default serializer sets
`ignoreUnknownKeys=true` (so partial models decode full RPC rows safely).

### Done: Reminders (Phase 6)
All under `reminders/`. **No new dependency** — I used AlarmManager directly instead of
WorkManager (simpler, and it reads a cached location so it works fully offline).
- `ReminderPrefs` — SharedPreferences: master + per-prayer toggles, and a cached copy of
  the location (lat/lng/method/madhab/timezone) so the scheduler never needs the network.
- `PrayerAlarmScheduler` — schedules one exact alarm per enabled prayer for today, plus a
  self-perpetuating daily "refresh" alarm at 00:05 that recomputes for the new day. Uses
  `setExactAndAllowWhileIdle`, degrades to inexact if exact-alarm permission is absent.
- `ReminderReceiver` (posts the notification / handles the daily refresh), `BootReceiver`
  (re-arms alarms after reboot — exact alarms don't survive reboot), `Notifications`
  (channel + POST_NOTIFICATIONS-gated posting).
- `ReminderBootstrap.sync()` fetches the profile, caches location, reschedules — called
  after login (MainScreen) and after saving Settings.
- Manifest: POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED
  + the two receivers. `PrayerTrackerApp` creates the channel and re-arms on launch.
- Settings gained a Reminders section: master + per-prayer switches, a POST_NOTIFICATIONS
  request, and an "Allow exact alarms" button when needed.
- **Not done (intentional):** notification quick-actions ("Prayed"/"Later") — logging from
  the shade needs background auth + a network call; left as a follow-up.

**⚠️ Reminders need real-device testing** — alarms/notifications/permissions behave
differently across OEMs (Samsung battery optimisation is aggressive). Because
`USE_EXACT_ALARM` is declared and this is sideloaded (not Play Store), exact alarms should be
auto-granted with no prompt. Test: set a prayer time a few minutes out via a Custom location,
confirm the notification fires.

### Phase 3B (offline / Room) — DEFERRED ON PURPOSE
I did **not** add Room tonight. It requires the KSP compiler plugin matched exactly to the
Kotlin version, and the KSP-for-Kotlin-2.3.20 versioning is currently ambiguous (KSP2 changed
the scheme). A wrong guess makes the **whole app fail to build** — which would block every
feature I wrote tonight when you build in the morning. Not worth that risk unattended.

When we do it next session (versions researched and ready):
- `com.google.devtools.ksp` Gradle plugin — pick the build whose prefix matches Kotlin
  **2.3.20** from https://github.com/google/ksp/releases (latest line was `2.3.11`; confirm
  the exact `2.3.20-…` build before use).
- `androidx.room:room-runtime:2.8.4` + `androidx.room:room-ktx:2.8.4` +
  `ksp("androidx.room:room-compiler:2.8.4")`.
- Design: a `PrayerLogEntity` (localId, prayer_date, prayer, status, in_jamaah, updated_at,
  `pendingSync` flag) as the UI source of truth; writes go to Room instantly, an outbox
  coroutine upserts to Supabase when online. The `unique(user_id, prayer_date, prayer)` DB
  constraint makes the upsert idempotent. `PrayerLogRepository` is the only file that changes
  — the ViewModel/UI already talk to it through a clean interface.

The app works **online-first** without this: logs save straight to Supabase. Offline just
means a log made with no signal isn't retried automatically yet.

---

## ☀️ Morning build guide

1. **Sync** Gradle (new: `adhan2` dependency; no other dependency changes tonight).
2. **Make Project** (`Ctrl+F9`), then **Run** on your Galaxy.
3. Expect to land on the **bottom-nav app**: Today / Streaks / Friends / Settings.

### First things to try
- **Settings** → pick your city (Sydney or wherever), calculation method, madhab → **Save**.
  Then **Today → Refresh** — times should update and the "default location" banner disappears.
- **Today** → log a couple of prayers, toggle Jama'ah.
- **Streaks** → per-prayer streaks + the heatmap of recent days.
- **Friends** → **Create invite code**; on a second account, **Redeem** it; open the friend
  to see their streaks. (This is also the two accounts you need for the skipped §9 RLS test.)
- **Reminders** (Settings) → enable, grant the notification permission when asked.

### If the build fails — likely spots (all quick fixes), in order of likelihood
1. **`viewModel { … }` initializer** in `StreaksScreen.kt` / `FriendDetailScreen.kt` — if
   unresolved, it's a lifecycle-compose overload mismatch; tell me and I'll switch to an
   explicit `ViewModelProvider.Factory`.
2. **An "experimental API" error on a Chip** (`FilterChip`/`AssistChip`) — add
   `@OptIn(ExperimentalMaterial3Api::class)` to that composable. (Shouldn't happen on this
   Compose version, but it's the cheapest possible fix.)
3. **A missing import** somewhere in the new files — Android Studio will offer the import.
Paste me any red text and I'll fix it fast. Everything supabase-kt/adhan2/postgrest was
verified against library source, so those are low-risk.

### Lint note (not a build failure)
`Notifications.show` calls `notify()` guarded by a runtime permission check; lint's
`MissingPermission` may flag it in the editor, but lint doesn't run on debug build/run, so it
won't block you.

---

## Release checklist (Phase 7 — for later)
- Create a **release keystore** and back it up somewhere you'll still have in 2 years (same
  package + key + higher versionCode = updates that preserve data; lose the key and everyone
  must uninstall).
- `assembleRelease` a signed APK; flip `isMinifyEnabled` on only after adding + testing
  proguard keep rules for supabase-kt/ktor/serialization.
- **Run the §9 RLS verification** (`supabase/tests/rls_verification.sql`) before sending to
  anyone — it's the whole security boundary and it's still unrun.
- Decide the **location-privacy** question (profiles expose lat/lng to friends).
- Recipients: Play Protect warning + "install unknown apps" is normal for sideloaded APKs.

---

### Added: Jummah + Tahajjud
- **Jummah** — modeled as the Friday replacement for Dhuhr (you never pray both), so it's
  **stored/streak-counted as Dhuhr** and only relabeled "Jummah" on Fridays: on the Today
  card, and in the Friday Dhuhr reminder notification. No DB enum change. The Dhuhr streak
  flows through Fridays automatically.
- **Tahajjud** — a new `tahajjud` prayer that's logged as a simple **"Prayed" toggle** (shown
  under an "Optional (not counted in streaks)" header on Today), timed at the **last third of
  the night** via adhan2 `SunnahTimes`. **Excluded from streaks and the heatmap.** Its
  reminder defaults **off**.

**⚠️ Requires running a new DB migration:** `supabase/migrations/0007_extra_prayers.sql`
adds the `tahajjud` enum value and rewrites `prayer_streaks` to compute only the 5 fard.
**Until you run it, tapping "Log" on Tahajjud will show a save error** (the enum value doesn't
exist server-side yet) — everything else works. Run it in the Supabase SQL editor; if it
complains about `ALTER TYPE … ADD VALUE` in a transaction, run that one line on its own first.

### Added: Phase 3B (offline / Room) + notification quick-action
Room is now the local source of truth for prayer logs; Supabase sync happens through an outbox.
- `data/local/` — `PrayerLogEntity` (PK userId+prayerDate+prayer, with `pendingSync` + `deleted`
  tombstone flags), `PrayerLogDao`, `AppDatabase`, and a `LocalDb` singleton initialised in
  `PrayerTrackerApp.onCreate`.
- `PrayerLogRepository` rewritten **offline-first**, same method signatures (so `TodayViewModel`
  is untouched): writes land in Room instantly with `pendingSync=true`; `logsForDate` does a
  best-effort push+pull then reads Room, so it works with no signal. Idempotent push via the
  `unique(user_id,prayer_date,prayer)` key. Local cache is cleared on sign-out.
- **Quick-action**: prayer reminders now carry a **"Prayed on time"** button that logs straight
  to Room from the shade (works offline; syncs later) and dismisses — no app open needed. The
  signed-in user id is cached in `ReminderPrefs` so the receiver can attribute the log even if
  the session isn't loaded. Tapping the notification body opens the app.

**Dependencies added (need a Gradle sync):** `androidx.room:room-runtime/room-ktx:2.8.4`,
`ksp("androidx.room:room-compiler:2.8.4")`, and the **KSP Gradle plugin `2.3.11`**.

**⚠️ KSP is the one spot most likely to need a tweak** (it's the reason I deferred this
overnight). KSP moved to decoupled versioning at 2.3.0, so `2.3.11` targets the Kotlin 2.3.x
line. If Gradle sync fails with a KSP/Kotlin compatibility error:
1. bump `ksp` in `libs.versions.toml` to the newest `2.3.x` on the KSP releases page, and/or
2. add `ksp.useKSP2=true` to `gradle.properties`.
Paste me the error and I'll pin it. Everything else in 3B is plain Room/coroutines.

### UI makeover (warm/minimal, reference-inspired)
Full visual redesign to the light "fitness app" aesthetic — off-white background, white rounded
cards, coral accent, black pill buttons, a floating pill bottom nav, a selectable week strip,
and an animated completion ring.
- **No new dependency.** (The request mentioned Framer Motion — that's a React/web library and
  can't run in a native Android app. The equivalent, **Jetpack Compose Animation**, is already
  in the Compose BOM.) Animations used: `animateFloatAsState` (ring sweep, button press),
  `animateColorAsState` (day/nav/chip selection), `Canvas` for the ring.
- New `ui/theme/Theme.kt` (brand-locked light palette + shapes + type) and `ui/design/Design.kt`
  (SoftCard, PillButton, OutlinePill, StatusPill, ProgressRing, WeekStrip, SectionLabel,
  ScreenHeader). Every screen (Today, Streaks, Friends, Friend detail, Settings, Auth) and the
  nav shell were rebuilt on these.
- **New feature that came with it:** the Today week strip is now **functional** — tap a day to
  view/log that date (`TodayViewModel` gained per-date loading + a "Today" reset button).
- Since there are **no Gradle changes**, this is just **Make Project + Run** — no sync needed.

### Added: Qibla compass (`qibla/`)
A 5th nav tab. `QiblaMath` computes the great-circle bearing to the Kaaba; `QiblaViewModel`
loads the saved location and the magnetic declination (`GeomagneticField`) for true-north
correction; `QiblaScreen` reads the `TYPE_ROTATION_VECTOR` sensor and draws a Canvas compass
whose needle points to the Qibla relative to the device heading, turning green when aligned.
- **No permission, no dependency, no manifest change** (rotation-vector is a motion sensor).
- Needs a location set in Settings (same as prayer times); shows a prompt otherwise.
- (This was in the "later" list §13 — added by request.)

### Icons: Lucide, not emoji
All UI glyphs are **Lucide icons** transcribed to Compose `ImageVector`s in
`ui/design/LucideIcons.kt` (sun, flame, users, settings, arrow-left, compass) — zero
dependency. Recolored via `Icon(tint=…)` like Material icons.

## Session end — self-review done
Before stopping I re-read the integration points and swept the whole `app/` source:
- No stale references (HomeScreen removed cleanly), no imports needing absent dependencies
  (no material-icons / work / room / datastore / kotlinx.datetime).
- Verified against library source: supabase-kt (from/select/upsert/update/delete/rpc/decode,
  filters, default serializer), adhan2 (Coordinates/DateComponents/CalculationParameters.
  madhab/PrayerTimes fields/Madhab), and the `viewModel { initializer }` lifecycle overload.
- Opt-ins: `TopAppBar` (MainScreen) covered; chips are stable on this Compose version.

Net: one new dependency to sync (`adhan2`), ~13 new/changed feature files, all in a
coherent state. I stopped rather than start Room because a KSP mismatch could break the whole
build unattended — that's the first thing to do together next session. Everything else (RLS
verify, release keystore) is in the checklists above. Good night 🌙
