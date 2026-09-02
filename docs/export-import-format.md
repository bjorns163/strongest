# Export / Import Formats

Strongest can move data in and out of the app in two different JSON formats:

| Format | Scope | Where in the app | Code | Sample |
|---|---|---|---|---|
| **Full backup** | Everything: exercises, routines, workout history, measurements, settings | Settings → *Export Data* / *Import Data* | [`utils/DataExportImport.kt`](../app/src/main/java/com/strongest/app/utils/DataExportImport.kt) | [`samples/backup-sample.json`](samples/backup-sample.json) |
| **Routine share** | A single routine and its planned sets | Routines → 3-dot menu → *Share* | [`utils/RoutineShare.kt`](../app/src/main/java/com/strongest/app/utils/RoutineShare.kt) | [`samples/routine-share-sample.json`](samples/routine-share-sample.json) |

Both are plain UTF-8 JSON, pretty-printed with a 2-space indent. Key order is not
significant. There is also an XLSX export in History — that one is a report for
spreadsheets, not an import format, and is not described here.

## General rules

- **Unknown keys are ignored.** A file written by a newer build still imports into an
  older one; the fields it does not know are dropped.
- **Missing keys fall back to defaults.** Every field below lists its default, so
  hand-written files can stay minimal.
- **Unknown enum values fall back** to the value marked *(fallback)* rather than
  failing the whole import.
- **Weights are always kilograms.** The kg/lbs setting only affects display, so a file
  exported in lbs mode still carries kg.
- **Timestamps are epoch milliseconds** (UTC).

## Enums

| Enum | Values |
|---|---|
| `setType` | `NORMAL` *(fallback)*, `WARM_UP`, `FAILURE`, `DROP_SET` |
| `muscleGroup` | `CHEST`, `BACK`, `SHOULDERS`, `BICEPS`, `TRICEPS`, `ABS`, `QUADS`, `HAMSTRINGS`, `GLUTES`, `CALVES`, `FOREARMS`, `LOWER_BACK`, `TRAPS`, `FULL_BODY`, `CARDIO`, `STRETCHING`, `OTHER` |
| `equipment` | `BARBELL`, `DUMBBELL`, `MACHINE`, `CABLE`, `BODYWEIGHT`, `KETTLEBELL`, `RESISTANCE_BAND`, `MEDICINE_BALL`, `SUSPENSION`, `SMITH_MACHINE`, `EZ_BAR`, `TRAP_BAR`, `PLATE`, `NONE`, `OTHER` |
| `classification` | `COMPOUND`, `ACCESSORY`, `ISOLATION` *(fallback)* |
| `metric` | `WEIGHT`, `BODY_FAT`, `CALORIC_INTAKE`, `NECK`, `SHOULDERS`, `CHEST`, `LEFT_BICEP`, `RIGHT_BICEP`, `LEFT_FOREARM`, `RIGHT_FOREARM`, `UPPER_ABS`, `WAIST`, `LOWER_ABS`, `HIPS`, `LEFT_THIGH`, `RIGHT_THIGH`, `LEFT_CALF`, `RIGHT_CALF` |
| `themeMode` | `LIGHT`, `DARK`, `SYSTEM` *(fallback)* |
| `weightUnit` | `KG` *(fallback)*, `LBS` |
| `oneRmFormula` | `EPLEY` *(fallback)*, `BRZYCKI`, `BOTH` |
| `userSex` | `MALE`, `FEMALE`, `UNSET` *(fallback)* |
| `caliperMode` | `THREE_SITE` *(fallback)*, `FOUR_SITE`, `SEVEN_SITE` |

`muscleGroup`, `equipment`, `classification` and `metric` are defined in
[`Exercise.kt`](../app/src/main/java/com/strongest/app/data/model/Exercise.kt) and
[`MeasurementEntry.kt`](../app/src/main/java/com/strongest/app/data/model/MeasurementEntry.kt);
those files are the authoritative lists.

`muscleGroup`, `equipment` and `metric` have **no fallback in the backup format** — an
unknown value there aborts the whole import. The routine share format is more forgiving
and falls back to `OTHER` / `NONE`.

---

# Full backup format

Sample: [`samples/backup-sample.json`](samples/backup-sample.json)

The backup is a faithful dump of the local database, ids included. **Importing is
destructive**: every table listed below is wiped and replaced with the file's contents,
and settings are overwritten. It is a restore, not a merge.

## Top level

| Key | Type | Default | Notes |
|---|---|---|---|
| `version` | int | `1` | Format version. Only `1` exists; the importer does not reject other values. |
| `exportedAt` | long | now | Epoch millis the file was written. |
| `source` | string | `""` | Always `com.strongest.app`. |
| `exercises` | array | `[]` | See [exercises](#exercises). |
| `routineGroups` | array | `[]` | |
| `routines` | array | `[]` | |
| `routineExercises` | array | `[]` | |
| `routineSets` | array | `[]` | Planned sets, **including warm-ups**. |
| `exerciseNotes` | array | `[]` | |
| `exerciseSettings` | array | `[]` | Per-exercise warm-up generator setting. |
| `workouts` | array | `[]` | |
| `workoutExercises` | array | `[]` | |
| `sets` | array | `[]` | Logged sets. |
| `measurementEntries` | array | `[]` | |
| `settings` | object | defaults | See [settings](#settings). |

Referential integrity is the file's responsibility: `routineExercises[].routineId` must
match a `routines[].id` in the same file, and so on down the chain. Foreign keys are
disabled during import, so a dangling reference is stored rather than reported.

## exercises

| Key | Type | Default | Notes |
|---|---|---|---|
| `id` | long | required | |
| `name` | string | required | |
| `muscleGroup` | enum | required | |
| `equipment` | enum | required | |
| `description` | string | `""` | |
| `instructions` | string | `""` | |
| `secondaryMuscles` | array of enum | `[]` | Muscle groups worked indirectly. |
| `imageUrl` | string | `""` | |
| `isCustom` | bool | `false` | `true` for user-created exercises. |
| `classification` | enum | `ISOLATION` | |

## routineGroups

| Key | Type | Default |
|---|---|---|
| `id` | long | required |
| `name` | string | required |
| `orderIndex` | int | `0` |
| `createdAt` | long | now |
| `updatedAt` | long | now |

## routines

| Key | Type | Default | Notes |
|---|---|---|---|
| `id` | long | required | |
| `name` | string | required | |
| `description` | string | `""` | |
| `groupId` | long or `null` | `null` | References `routineGroups[].id`. |
| `createdAt` | long | now | |
| `updatedAt` | long | now | |

## routineExercises

| Key | Type | Default | Notes |
|---|---|---|---|
| `id` | long | required | Referenced by `routineSets[].routineExerciseId`. |
| `routineId` | long | required | |
| `exerciseId` | long | required | |
| `orderIndex` | int | `0` | Position within the routine. |
| `defaultSets` | int | `3` | Used when the routine has no explicit `routineSets`. |
| `defaultReps` | int | `10` | |
| `defaultWeight` | float (kg) | `0` | |
| `restSeconds` | int | `90` | |

## routineSets

The planned sets of a routine. A warm-up set is an ordinary entry with
`"setType": "WARM_UP"`.

| Key | Type | Default | Notes |
|---|---|---|---|
| `id` | long | required | |
| `routineExerciseId` | long | required | |
| `setNumber` | int | `1` | 1-based, counted across all set types — warm-ups come first and the working sets continue the numbering. |
| `weight` | float (kg) | `0` | |
| `reps` | int | `10` | |
| `restSeconds` | int | `90` | |
| `setType` | enum | `NORMAL` | `WARM_UP` here is what makes a set a warm-up. |

## exerciseNotes

| Key | Type | Default | Notes |
|---|---|---|---|
| `exerciseId` | long | required | Primary key — one note per exercise. |
| `noteText` | string | `""` | |
| `updatedAt` | long | now | |

## exerciseSettings

Per-exercise preferences. Currently only the warm-up calculator's set count.

| Key | Type | Default | Notes |
|---|---|---|---|
| `exerciseId` | long | required | Primary key. |
| `warmUpSetCount` | int | `3` | How many warm-up sets the calculator generates (1–4). |

## workouts

| Key | Type | Default | Notes |
|---|---|---|---|
| `id` | long | required | |
| `routineId` | long or `null` | `null` | `null` for an empty (ad-hoc) workout. |
| `routineName` | string or `null` | `null` | Snapshot of the name at the time of the workout. |
| `workoutName` | string or `null` | `null` | User-set title. |
| `startTime` | long | now | |
| `endTime` | long or `null` | `null` | `null` while a workout is unfinished. |
| `notes` | string | `""` | |
| `isOngoing` | bool | `true` | |

## workoutExercises

| Key | Type | Default |
|---|---|---|
| `id` | long | required |
| `workoutId` | long | required |
| `exerciseId` | long | required |
| `orderIndex` | int | `0` |
| `notes` | string | `""` |

## sets

Logged sets of a performed workout.

| Key | Type | Default | Notes |
|---|---|---|---|
| `id` | long | required | |
| `workoutExerciseId` | long | required | |
| `setNumber` | int | `1` | |
| `weightKg` | float (kg) | `0` | Note the name differs from `routineSets[].weight`. |
| `reps` | int | `0` | |
| `rpe` | float or `null` | `null` | 1–10, only when RPE tracking is on. |
| `setType` | enum | `NORMAL` | `WARM_UP` sets are excluded from PRs and progress stats. |
| `restSeconds` | int | `90` | |
| `completedAt` | long | now | |

## measurementEntries

| Key | Type | Default |
|---|---|---|
| `id` | long | required |
| `metric` | enum | required |
| `value` | float | `0` |
| `timestamp` | long | now |
| `notes` | string | `""` |

## settings

| Key | Type | Default | Notes |
|---|---|---|---|
| `themeMode` | enum | `SYSTEM` | |
| `weightUnit` | enum | `KG` | Display only. |
| `defaultRestSeconds` | int | `90` | |
| `timerAdjustmentSeconds` | int | `30` | Step size of the +/- rest timer buttons. |
| `lastSetRestSeconds` | int | `150` | Rest after the final set of an exercise. |
| `keepScreenOn` | bool | `false` | |
| `notificationSoundUri` | string or `null` | `null` | Android sound URI; may not resolve on another device. |
| `rpeTrackingEnabled` | bool | `false` | |
| `workoutNotificationEnabled` | bool | `true` | |
| `availableKgPlates` | object | `{}` | **String→string** map of plate weight to count, e.g. `{"20.0": "999"}`. `999` means "effectively unlimited". |
| `availableLbsPlates` | object | `{}` | Same shape, in pounds. |
| `oneRmFormula` | enum | `EPLEY` | |
| `recoveryHoursByMuscle` | object | `{}` | **String→string** map of muscle group to hours, e.g. `{"CHEST": "48"}`. |
| `userSex` | enum | `UNSET` | |
| `birthYear` | int | `0` | `0` means unset. |
| `caliperMode` | enum | `THREE_SITE` | |

The two plate maps and `recoveryHoursByMuscle` store their **values as strings**, not
numbers — that is what the app writes and what it expects to read back. Entries whose
key or value does not parse are skipped silently.

---

# Routine share format

Sample: [`samples/routine-share-sample.json`](samples/routine-share-sample.json)

A single routine, small enough to send through the Android share sheet as text.
Importing a shared routine **adds** a routine; it never overwrites existing data.

Unlike the backup, this format carries no database ids. Exercises travel by **name**:
on import, a case-insensitive name match against the library is reused, and anything
unmatched is created as a custom exercise with the muscle group and equipment from the
file. Routine group membership is not part of the format — an imported routine is
ungrouped.

## Top level

| Key | Type | Default | Notes |
|---|---|---|---|
| `version` | int | `1` | **A file whose version is not `1` is rejected outright.** |
| `name` | string | required | Routine name. |
| `description` | string | `""` | |
| `exercises` | array | required | |

## exercises[]

| Key | Type | Default | Notes |
|---|---|---|---|
| `name` | string | required | Match key against the exercise library. |
| `muscleGroup` | enum | `OTHER` | Only used when the exercise has to be created. |
| `equipment` | enum | `NONE` | Only used when the exercise has to be created. |
| `sets` | array | `[]` | See below. Order in the file is the order in the routine. |
| `defaultSets` | int | `3` | |
| `defaultWeight` | float (kg) | `0` | |
| `defaultReps` | int | `10` | |
| `restSeconds` | int | `90` | Also the fallback for a set that omits `restSeconds`. |

## exercises[].sets[]

| Key | Type | Default | Notes |
|---|---|---|---|
| `setNumber` | int | position in the array | 1-based, counted across all set types. |
| `weight` | float (kg) | `0` | |
| `reps` | int | `10` | |
| `restSeconds` | int | the exercise's `restSeconds` | |
| `setType` | enum | `NORMAL` | `WARM_UP`, `FAILURE` and `DROP_SET` survive a share/import round trip. |

`setType` was added after the first release. Files written before it exist without the
key and import as plain working sets, which is why the version stayed at `1`.
