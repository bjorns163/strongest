# Strongest - Free Android Workout Tracker

A completely free Android workout tracker with **500+ exercises** and no paywalls.

## Features

### Exercise Library
- **515 exercises** across 17 muscle groups
- **Exercise classification** — every exercise tagged as Compound, Accessory, or Isolation
- **Filter by muscle group & equipment** — quickly narrow down exercises
- **Search** by name
- **Create custom exercises** — name, muscle group, equipment, classification, and instructions
- **Usage counts** — see how often you've used each exercise
- **Exercise notes** — per-exercise freeform notes saved across sessions

### Exercise Details
- **Exercise info** — muscle group, equipment, classification, instructions
- **History tab** — full set-by-set history with dates, weights, reps, and RPE
- **1RM calculator tab** — enter weight and reps to calculate estimated 1-rep max using Epley or Brzycki formula; shows a reps-vs-weight table (1–12 reps) based on the result; auto-filled from your latest session
- **Warm-up calculator tab** — enter your working weight and reps; generates percentage-based warm-up sets (50%, 70%, 85%) with appropriate rep counts; auto-filled from your latest session
- **Edit exercise** — name, muscle group, equipment, classification, instructions for custom exercises

### Workout Logging
- **Start empty workout** or launch from a routine
- **Add exercises** with multi-select exercise picker (search + filters)
- **Replace exercise** mid-workout
- **Set tracking** — log weight, reps, and set type (normal, warm-up, drop set, failure)
- **RPE rating** (optional, toggle in Settings) — rates each set 1–10 with a brief description; auto-prompts after tapping the check button
- **Previous session data** shown inline while logging so you can beat your last performance
- **Rest timer** — auto-starts after completing a set
- **Plate calculator** — accessible from the 3-dot menu on any exercise; shows plates needed per side for target weight; supports kg and lbs; respects your available plates from Settings
- **Reorder exercises** and sets
- **Delete exercises** and sets
- **Finish or discard** workout

### Routines
- **Unlimited routines** with custom names and descriptions
- **Routine groups** — organise routines into named groups
- **Build routines** from the full exercise library
- **Launch a workout directly** from a routine

### Progress & History
- **Workout history** — browse all past workouts
- **Personal records** tracked automatically
- **Per-exercise history** with volume and max weight stats
- **Total sets and total workouts** summary per exercise

### Body Measurements
- Log and track body measurements over time

### Settings
- **Theme** — Light, Dark, or System default
- **Weight unit** — kg or lbs (affects all input and display)
- **Rest timer** — default rest seconds, last set rest seconds, and timer adjustment step
- **Keep screen on** — prevent screen sleep during active workouts
- **RPE tracking** — turn on/off; when on, a pop-up appears after each completed set
- **Timer notification sound** — pick a custom sound for rest timer alerts
- **Plate calculator defaults** — select which plates are available in your gym (separate presets for kg and lbs); 2-column grid layout
- **1RM formula** — choose Epley, Brzycki, or show both
- **Recovery hours** — per-muscle-group recovery windows shown on the Progress tab
- **Profile** — gender, birth year, and caliper method for accurate body fat calculations
- **XLSX export** — select workouts in History and share them as an Excel file

### General
- **Material 3** design
- **Dark & light theme**
- **Completely free** — no ads, no subscriptions, no paywalls

---

## Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Room** — local database with migrations
- **Hilt** — dependency injection
- **DataStore** — preferences
- **Material 3** design system
- **MVVM** architecture
- **Target SDK 35** (Android 15)
- **Min SDK 26** (Android 8.0)

---

## Project Structure

```
app/src/main/java/com/strongest/app/
├── data/
│   ├── db/              # Room database, DAOs, TypeConverters, migrations
│   ├── model/           # Data classes (Exercise, Workout, Set, Routine, …)
│   └── repository/      # WorkoutRepository, SettingsRepository, seed data
├── di/                  # Hilt modules
└── ui/
    ├── exercise/        # Exercise library, detail, picker, custom exercise dialog
    ├── history/         # Workout history
    ├── measurements/    # Body measurements
    ├── navigation/      # Nav graph
    ├── progress/        # Progress & PRs
    ├── routines/        # Routine builder & groups
    ├── settings/        # Settings screen
    ├── theme/           # Material 3 theme
    └── workout/         # Active workout, plate calculator, WorkoutViewModel
```

---

## Build Instructions

### Prerequisites
1. Android Studio Ladybug (2024.2) or newer
2. JDK 17
3. Android SDK 35

### Setup
1. Open the project in Android Studio
2. Sync Gradle and build

### Build APK
```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release AAB (signed)
```

### Run Tests
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

---

## Publishing to Google Play

1. **Register Developer Account** — $25 one-time fee at [Google Play Console](https://play.google.com/console)
2. **Generate signed AAB**:
   ```bash
   ./gradlew bundleRelease
   ```
3. **Complete Store Listing** — icon (512×512), feature graphic (1024×500), screenshots, description
4. **Content Rating** — complete the IARC questionnaire
5. **Data Safety** — declare data practices (local-only = minimal)
6. **Testing Track** — 20 testers for 14 days required for new personal accounts
7. **Submit for Review**

---

## License

This project is open source under the MIT License.

---

## Privacy Statement

**Strongest** does not collect, store, transmit, or share any personal data.

- **Local-only storage** — all data (workouts, exercises, routines, measurements, settings) is stored exclusively on your device using Room (local database) and DataStore (preferences).
- **No internet access** — the app requires no internet permission. It never connects to remote servers, APIs, or cloud services.
- **No analytics or crash reporting** — no third-party analytics, tracking, or telemetry SDKs are included.
- **No ads** — the app contains no advertising SDKs.
- **No user accounts** — there is no login, registration, or profile synced to a server.
- **No data sharing** — the app does not share data with any third parties. Data export (XLSX) is user-initiated and saved locally or shared via Android's share sheet — the app itself never sends data externally.

### Data Safety (Google Play)

When publishing, your Google Play Data Safety section should declare:
- **Data collected**: None
- **Data shared**: None
- **Data encrypted in transit**: Not applicable (no network communication)
- **Data deletion**: Uninstall the app to remove all stored data

### Your Control

You can view, modify, or delete all data directly within the app. To completely remove all data, clear the app's storage in Android Settings or uninstall the application.

