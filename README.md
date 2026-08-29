# Prayer Tracker

A native Android app for tracking the five daily prayers, Jummah, and Tahajjud, with
per-prayer streaks, a Qibla compass, prayer-time reminders, and friend-to-friend
accountability — invite a friend by code and see each other's streaks.

- **Client:** Kotlin + Jetpack Compose, offline-first (Room + sync outbox), sideloaded APK.
- **Backend:** hosted Supabase (Postgres + Auth + Row Level Security). No custom server.

See [`android/README.md`](android/README.md) for the app's dev setup and
[`supabase/README.md`](supabase/README.md) for the database schema, RLS policies, and how
to apply migrations.

## Installing (for friends)

You'll get a one-time invite to install the app, then updates arrive automatically —
no more re-downloading a file every time a new version ships.

1. **Install [Obtainium](https://github.com/ImranR98/Obtainium)** — an app that tracks other
   apps' GitHub Releases and notifies you when an update is out (itself distributed the same
   way, outside the Play Store — grab its APK from
   [Obtainium's own releases page](https://github.com/ImranR98/Obtainium/releases)).
2. Open Obtainium → **Add App** → paste this repo's URL:
   ```
   https://github.com/AR13X3/Prayer_Tracker
   ```
3. Obtainium finds the latest release automatically. Tap **Add**, then **Install**.
4. From then on, Obtainium checks for new releases in the background (or pull-to-refresh
   any time) and prompts you to update in place — your data isn't affected, since Android
   updates preserve app storage when the signing key matches.

You can also see the current version anytime inside the app: **Settings → About**, which
also has a **"View releases on GitHub"** shortcut if you want to check manually.

## Releasing a new version (maintainer)

```bash
scripts/release.sh 0.3.0    # bumps versionName + versionCode, commits, tags locally
git push --follow-tags      # pushes the tag, which triggers the build
```

Pushing the tag runs [`.github/workflows/release.yml`](.github/workflows/release.yml): it
builds a signed release APK and publishes it as a GitHub Release attached to that tag.
Obtainium (and anyone's in-app "View releases" link) picks it up from there — no manual
upload step.

**One-time setup required** before the first tag push — add these as
**Settings → Secrets and variables → Actions → Repository secrets** on GitHub:

| Secret | Value |
|---|---|
| `SUPABASE_URL` | your Supabase project URL |
| `SUPABASE_ANON_KEY` | the **anon/publishable** key — never `service_role` |
| `RELEASE_KEYSTORE_BASE64` | your release `.jks`/`.keystore` file, base64-encoded |
| `RELEASE_KEYSTORE_PASSWORD` | keystore password |
| `RELEASE_KEY_ALIAS` | key alias (e.g. `key0`) |
| `RELEASE_KEY_PASSWORD` | key password |

To base64-encode the keystore (run locally, never paste the output anywhere but the GitHub
secret field):

```bash
# PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\release.jks")) | Set-Clipboard
# then paste directly into the GitHub secret value field
```

The tag you push must match `app/build.gradle.kts`'s `versionName` exactly (`scripts/release.sh`
guarantees this) — the workflow refuses to build otherwise, so a forgotten version bump fails
loudly instead of shipping a release that looks like nothing changed.
