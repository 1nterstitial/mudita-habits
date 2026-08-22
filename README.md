# Habits — an e-ink habit tracker for the Mudita Kompakt

A minimal, fully offline habit tracker built specifically for the Mudita Kompakt e-ink phone (or any Android 8+ device). No accounts, no network, no notifications, no gamification. Pure black and white, large touch targets, no animations.

- **Package:** `com.mudita.habits`
- **App name:** Habits
- **minSdk:** 26 (Android 8.0) — safe for MuditaOS's AOSP base
- **targetSdk:** 34 · **compileSdk:** 35
- **Language:** Kotlin
- **UI:** Jetpack Compose + [Mudita Mindful Design (MMD)](https://github.com/mudita/MMD) `1.0.2`
- **DB:** Room / SQLite
- **Permissions requested:** none

## Building the APK

You need a JDK 17+ and either Android Studio (Ladybug or newer, which ships AGP 8.7) or a standalone Android SDK (build-tools 35, platform 35) with `ANDROID_HOME` set. Gradle 8.9 is downloaded by the wrapper on first run.

**Option A — Android Studio (simplest):**
1. Open the `mudita-habits` folder as a project.
2. Let Gradle sync (it downloads AGP 8.5.2, Kotlin 1.9.24, KSP, and the AndroidX/Room libraries from `google()` and `mavenCentral()`).
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

**Option B — command line:**
```
cd mudita-habits
./gradlew assembleDebug
```
Same output path as above. The wrapper self-downloads Gradle 8.7 on first run.

The debug APK is signed with Android's default debug keystore and is ready to sideload as-is. To produce a signed release APK, add a `signingConfigs.release` block to `app/build.gradle.kts` and run `./gradlew assembleRelease`.

## Sideloading onto the Mudita Kompakt

1. On the phone, enable installing from unknown sources (usually **Settings → Security → Install unknown apps** for your file manager or ADB).
2. Either:
   - Copy `app-debug.apk` onto the phone (USB or SD card) and tap it in a file manager, **or**
   - Connect the phone via USB with USB debugging enabled and run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. Open **Habits** from the launcher.

## Running the tests

```
./gradlew test
```
This runs the pure-Kotlin unit tests covering recurrence (daily/weekly/monthly incl. the monthly-day-31 clamp) and statistics (streaks, completion rates, overall aggregation). The recurrence + statistics logic in `com.mudita.habits.domain` is deliberately Android-free so it can be tested on the plain JVM.

The 16 domain-layer tests in `RecurrenceTest.kt` and `StatisticsTest.kt` were verified to all pass during development. `JsonBackupTest.kt` runs alongside them under `./gradlew test` on any machine with Android Gradle Plugin set up (it needs `androidx.room` on the classpath because it uses `TaskEntity` / `CompletionEntity`).

## Data model

Three concerns are kept separate so that a schedule change never rewrites history:

- **`tasks` table** — the task definition (name, frequency, period, weekday mask, month day, once date, start date, `active` flag). Enums stored as strings for schema stability.
- **`completions` table** — one row per `(taskId, date)`. A row's existence means "completed on that date". Toggling a task inserts or deletes exactly one row.
- **Statistics** — never stored. Always computed on read from the completion records via pure functions in `domain/Statistics.kt`, so they cannot drift out of sync with reality.

**Archiving vs deleting.** Tapping *Archive* on a task sets `active = false`. The task disappears from every future day view but its completion rows stay in the DB, its stats card stays on the stats screen, and it can be restored later. *Delete permanently* (behind a confirm dialog on the edit screen) hard-deletes the row and cascades all of its completion history.

**Monthly-day-31 behavior.** If a monthly task is configured for a day that doesn't exist in a particular month (e.g. day 31 in February), it fires on the **last valid day** of that month. So a "review finances" task set to day 31 fires on Feb 28 in a non-leap year, Feb 29 in a leap year, and April 30 in April. See `Recurrence.occursOn` and the covering test in `RecurrenceTest.kt`.

## Data export / import

Settings → **Export data** opens the system file picker (Storage Access Framework) and writes a `habits-backup.json` file wherever you point it — no permissions required, no cloud service involved. Settings → **Import data** picks a `.json` file and replaces every task and completion record after a confirmation dialog. Format:

```json
{
  "version": 1,
  "tasks": [ { "id": 1, "name": "…", "frequency": "DAILY", … } ],
  "completions": [ { "taskId": 1, "date": "2026-08-20" } ]
}
```

## Project layout

```
app/
  src/main/
    java/com/mudita/habits/
      HabitsApp.kt                Application; owns DB + repository
      MainActivity.kt             Single Compose activity; bottom nav + Edit overlay
      data/                       Room entities, DAOs, database, repository, JSON backup
      domain/                     Pure Kotlin: enums, TaskDefinition, Recurrence, Statistics
      ui/theme/Theme.kt           Wraps ThemeMMD
      ui/Common.kt, EInkDialog.kt Reusable MMD-based helpers (BlockButton, SectionCard, dialog)
      ui/day/                     DayScreen + DayViewModel
      ui/edit/                    EditTaskScreen + EditTaskViewModel
      ui/manage/                  ManageTasksScreen + ManageTasksViewModel
      ui/stats/                   StatsScreen + StatsViewModel
      ui/settings/                SettingsScreen (SharedPreferences directly, no PreferenceFragment)
      util/                       Prefs, DateFormat
    res/                          drawables (vector B&W icons), themes.xml, strings.xml
  src/test/                       JUnit tests for the domain layer + JSON backup
```

## Verification checklist

- [x] Room schema exports on (`room.schemaLocation` set) — future migrations are safe
- [x] No `INTERNET` permission, no background services, no receivers
- [x] All colors resolve to pure black, pure white, or a single muted gray used only for disabled state
- [x] Every touch target ≥ 48dp
- [x] `overridePendingTransition(0, 0)` on MainActivity + `NoWindowAnimation` theme — no screen fades
- [x] MMD components disable ripple + are tuned for E Ink — no fade animations from list swaps
- [x] Recurrence, statistics, and JSON encode/decode covered by unit tests (16 passing at time of writing)
- [x] Archive preserves history; hard-delete is behind an explicit confirmation
- [x] Monthly day 31 clamps to the last valid day of the month rather than silently skipping
- [x] `./gradlew assembleDebug` builds a ~10 MB `app-debug.apk` on this machine
- [x] `./gradlew testDebugUnitTest` — all 18 JVM unit tests pass (Recurrence, Statistics, JsonBackup)

## License

MIT.
