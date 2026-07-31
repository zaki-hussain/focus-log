# Focus Log

A punch-clock for deep work. Pick a minimum focus time, tap Start, put the phone away.
Sessions accumulate into a permanent local log. No accounts, no cloud, no notifications,
no gamification — a ledger with a big button.

## App

Native Android (Kotlin + Jetpack Compose foundation — no Material, no third-party libraries).

- `app/src/main/java/app/focuslog/Store.kt` — data model, SharedPreferences persistence, formatting
- `app/src/main/java/app/focuslog/MainActivity.kt` — activity, design tokens, shared composables
- `app/src/main/java/app/focuslog/Clock.kt` — home screen (minimum dial, tags, Start/Cancel/Terminate/Stop)
- `app/src/main/java/app/focuslog/Stats.kt` — totals, native charts, heatmap, history, CSV export

The active session is just a stored start timestamp — elapsed time is computed from the
wall clock, so sessions survive app kills and reboots with no background service. All
stats are derived from the log at read time. Failed sessions (ended before the minimum)
are logged but excluded from totals.

Build: `./gradlew assembleDebug` (needs the Android SDK; `minSdk 26`).

## Design source

- `project/` — original Claude Design HTML prototypes
- `chats/` — design conversation transcript
