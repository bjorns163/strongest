package com.strongest.app.utils

import com.strongest.app.data.model.BodyMetric
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseClassification
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.MeasurementEntry
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SetLog
import com.strongest.app.data.model.SetType
import com.strongest.app.data.model.Workout
import com.strongest.app.data.model.WorkoutExercise
import com.strongest.app.data.repository.AppSettings
import com.strongest.app.data.repository.CaliperMode
import com.strongest.app.data.repository.OneRmFormula
import com.strongest.app.data.repository.Sex
import com.strongest.app.data.repository.WeightUnit
import org.json.JSONArray
import org.json.JSONObject

data class ExportData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val source: String = "com.strongest.app",
    val exercises: List<Exercise> = emptyList(),
    val routineGroups: List<RoutineGroup> = emptyList(),
    val routines: List<Routine> = emptyList(),
    val routineExercises: List<RoutineExercise> = emptyList(),
    val routineSets: List<RoutineSet> = emptyList(),
    val exerciseNotes: List<ExerciseNote> = emptyList(),
    val workouts: List<Workout> = emptyList(),
    val workoutExercises: List<WorkoutExercise> = emptyList(),
    val sets: List<SetLog> = emptyList(),
    val measurementEntries: List<MeasurementEntry> = emptyList(),
    val settings: AppSettings = AppSettings()
)

private const val KEY_VERSION = "version"
private const val KEY_EXPORTED_AT = "exportedAt"
private const val KEY_SOURCE = "source"
private const val KEY_EXERCISES = "exercises"
private const val KEY_ROUTINE_GROUPS = "routineGroups"
private const val KEY_ROUTINES = "routines"
private const val KEY_ROUTINE_EXERCISES = "routineExercises"
private const val KEY_ROUTINE_SETS = "routineSets"
private const val KEY_EXERCISE_NOTES = "exerciseNotes"
private const val KEY_WORKOUTS = "workouts"
private const val KEY_WORKOUT_EXERCISES = "workoutExercises"
private const val KEY_SETS = "sets"
private const val KEY_MEASUREMENT_ENTRIES = "measurementEntries"
private const val KEY_SETTINGS = "settings"

private const val SET_THEME_MODE = "themeMode"
private const val SET_WEIGHT_UNIT = "weightUnit"
private const val SET_DEFAULT_REST_SECONDS = "defaultRestSeconds"
private const val SET_TIMER_ADJUSTMENT_SECONDS = "timerAdjustmentSeconds"
private const val SET_LAST_SET_REST_SECONDS = "lastSetRestSeconds"
private const val SET_KEEP_SCREEN_ON = "keepScreenOn"
private const val SET_NOTIFICATION_SOUND_URI = "notificationSoundUri"
private const val SET_RPE_TRACKING_ENABLED = "rpeTrackingEnabled"
private const val SET_AVAILABLE_KG_PLATES = "availableKgPlates"
private const val SET_AVAILABLE_LBS_PLATES = "availableLbsPlates"
private const val SET_ONE_RM_FORMULA = "oneRmFormula"
private const val SET_RECOVERY_HOURS_BY_MUSCLE = "recoveryHoursByMuscle"
private const val SET_USER_SEX = "userSex"
private const val SET_BIRTH_YEAR = "birthYear"
private const val SET_CALIPER_MODE = "caliperMode"

private fun Exercise.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("muscleGroup", muscleGroup.name)
    put("equipment", equipment.name)
    put("description", description)
    put("instructions", instructions)
    put("secondaryMuscles", JSONArray(secondaryMuscles.map { it.name }))
    put("imageUrl", imageUrl)
    put("isCustom", isCustom)
    put("classification", classification.name)
}

private fun JSONObject.toExercise(): Exercise = Exercise(
    id = getLong("id"),
    name = getString("name"),
    muscleGroup = MuscleGroup.valueOf(getString("muscleGroup")),
    equipment = Equipment.valueOf(getString("equipment")),
    description = optString("description", ""),
    instructions = optString("instructions", ""),
    secondaryMuscles = optJSONArray("secondaryMuscles")?.let { arr ->
        (0 until arr.length()).map { MuscleGroup.valueOf(arr.getString(it)) }
    } ?: emptyList(),
    imageUrl = optString("imageUrl", ""),
    isCustom = optBoolean("isCustom", false),
    classification = try {
        ExerciseClassification.valueOf(optString("classification", "ISOLATION"))
    } catch (_: Exception) {
        ExerciseClassification.ISOLATION
    }
)

private fun RoutineGroup.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("orderIndex", orderIndex)
    put("createdAt", createdAt)
    put("updatedAt", updatedAt)
}

private fun JSONObject.toRoutineGroup(): RoutineGroup = RoutineGroup(
    id = getLong("id"),
    name = getString("name"),
    orderIndex = optInt("orderIndex", 0),
    createdAt = optLong("createdAt", System.currentTimeMillis()),
    updatedAt = optLong("updatedAt", System.currentTimeMillis())
)

private fun Routine.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("description", description)
    put("groupId", groupId ?: JSONObject.NULL)
    put("createdAt", createdAt)
    put("updatedAt", updatedAt)
}

private fun JSONObject.toRoutine(): Routine = Routine(
    id = getLong("id"),
    name = getString("name"),
    description = optString("description", ""),
    groupId = if (isNull("groupId")) null else optLong("groupId"),
    createdAt = optLong("createdAt", System.currentTimeMillis()),
    updatedAt = optLong("updatedAt", System.currentTimeMillis())
)

private fun RoutineExercise.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("routineId", routineId)
    put("exerciseId", exerciseId)
    put("orderIndex", orderIndex)
    put("defaultSets", defaultSets)
    put("defaultReps", defaultReps)
    put("defaultWeight", defaultWeight.toDouble())
    put("restSeconds", restSeconds)
}

private fun JSONObject.toRoutineExercise(): RoutineExercise = RoutineExercise(
    id = getLong("id"),
    routineId = getLong("routineId"),
    exerciseId = getLong("exerciseId"),
    orderIndex = optInt("orderIndex", 0),
    defaultSets = optInt("defaultSets", 3),
    defaultReps = optInt("defaultReps", 10),
    defaultWeight = optDouble("defaultWeight", 0.0).toFloat(),
    restSeconds = optInt("restSeconds", 90)
)

private fun RoutineSet.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("routineExerciseId", routineExerciseId)
    put("setNumber", setNumber)
    put("weight", weight.toDouble())
    put("reps", reps)
    put("restSeconds", restSeconds)
}

private fun JSONObject.toRoutineSet(): RoutineSet = RoutineSet(
    id = getLong("id"),
    routineExerciseId = getLong("routineExerciseId"),
    setNumber = optInt("setNumber", 1),
    weight = optDouble("weight", 0.0).toFloat(),
    reps = optInt("reps", 10),
    restSeconds = optInt("restSeconds", 90)
)

private fun ExerciseNote.toJson(): JSONObject = JSONObject().apply {
    put("exerciseId", exerciseId)
    put("noteText", noteText)
    put("updatedAt", updatedAt)
}

private fun JSONObject.toExerciseNote(): ExerciseNote = ExerciseNote(
    exerciseId = getLong("exerciseId"),
    noteText = optString("noteText", ""),
    updatedAt = optLong("updatedAt", System.currentTimeMillis())
)

private fun Workout.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("routineId", routineId ?: JSONObject.NULL)
    put("routineName", routineName ?: JSONObject.NULL)
    put("workoutName", workoutName ?: JSONObject.NULL)
    put("startTime", startTime)
    put("endTime", endTime ?: JSONObject.NULL)
    put("notes", notes)
    put("isOngoing", isOngoing)
}

private fun JSONObject.toWorkout(): Workout = Workout(
    id = getLong("id"),
    routineId = if (isNull("routineId")) null else optLong("routineId"),
    routineName = if (isNull("routineName")) null else optString("routineName"),
    workoutName = if (isNull("workoutName")) null else optString("workoutName"),
    startTime = optLong("startTime", System.currentTimeMillis()),
    endTime = if (isNull("endTime")) null else optLong("endTime"),
    notes = optString("notes", ""),
    isOngoing = optBoolean("isOngoing", true)
)

private fun WorkoutExercise.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("workoutId", workoutId)
    put("exerciseId", exerciseId)
    put("orderIndex", orderIndex)
    put("notes", notes)
}

private fun JSONObject.toWorkoutExercise(): WorkoutExercise = WorkoutExercise(
    id = getLong("id"),
    workoutId = getLong("workoutId"),
    exerciseId = getLong("exerciseId"),
    orderIndex = optInt("orderIndex", 0),
    notes = optString("notes", "")
)

private fun SetLog.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("workoutExerciseId", workoutExerciseId)
    put("setNumber", setNumber)
    put("weightKg", weightKg.toDouble())
    put("reps", reps)
    put("rpe", rpe?.toDouble() ?: JSONObject.NULL)
    put("setType", setType.name)
    put("restSeconds", restSeconds)
    put("completedAt", completedAt)
}

private fun JSONObject.toSetLog(): SetLog = SetLog(
    id = getLong("id"),
    workoutExerciseId = getLong("workoutExerciseId"),
    setNumber = optInt("setNumber", 1),
    weightKg = optDouble("weightKg", 0.0).toFloat(),
    reps = optInt("reps", 0),
    rpe = if (isNull("rpe")) null else optDouble("rpe").toFloat(),
    setType = try {
        SetType.valueOf(optString("setType", "NORMAL"))
    } catch (_: Exception) {
        SetType.NORMAL
    },
    restSeconds = optInt("restSeconds", 90),
    completedAt = optLong("completedAt", System.currentTimeMillis())
)

private fun MeasurementEntry.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("metric", metric.name)
    put("value", value.toDouble())
    put("timestamp", timestamp)
    put("notes", notes)
}

private fun JSONObject.toMeasurementEntry(): MeasurementEntry = MeasurementEntry(
    id = getLong("id"),
    metric = BodyMetric.valueOf(getString("metric")),
    value = optDouble("value", 0.0).toFloat(),
    timestamp = optLong("timestamp", System.currentTimeMillis()),
    notes = optString("notes", "")
)

private fun AppSettings.toJson(): JSONObject = JSONObject().apply {
    put(SET_THEME_MODE, themeMode.name)
    put(SET_WEIGHT_UNIT, weightUnit.name)
    put(SET_DEFAULT_REST_SECONDS, defaultRestSeconds)
    put(SET_TIMER_ADJUSTMENT_SECONDS, timerAdjustmentSeconds)
    put(SET_LAST_SET_REST_SECONDS, lastSetRestSeconds)
    put(SET_KEEP_SCREEN_ON, keepScreenOn)
    put(SET_NOTIFICATION_SOUND_URI, notificationSoundUri ?: JSONObject.NULL)
    put(SET_RPE_TRACKING_ENABLED, rpeTrackingEnabled)
    put(SET_AVAILABLE_KG_PLATES, JSONArray(availableKgPlates.map { it.toDouble() }))
    put(SET_AVAILABLE_LBS_PLATES, JSONArray(availableLbsPlates.map { it.toDouble() }))
    put(SET_ONE_RM_FORMULA, oneRmFormula.name)
    put(
        SET_RECOVERY_HOURS_BY_MUSCLE,
        JSONObject(recoveryHoursByMuscle.mapKeys { it.key.name }.mapValues { it.value.toString() })
    )
    put(SET_USER_SEX, userSex.name)
    put(SET_BIRTH_YEAR, birthYear)
    put(SET_CALIPER_MODE, caliperMode.name)
}

private fun JSONObject.toAppSettings(): AppSettings = AppSettings(
    themeMode = try {
        com.strongest.app.ThemeMode.valueOf(optString(SET_THEME_MODE, "SYSTEM"))
    } catch (_: Exception) {
        com.strongest.app.ThemeMode.SYSTEM
    },
    weightUnit = try {
        WeightUnit.valueOf(optString(SET_WEIGHT_UNIT, "KG"))
    } catch (_: Exception) {
        WeightUnit.KG
    },
    defaultRestSeconds = optInt(SET_DEFAULT_REST_SECONDS, 90),
    timerAdjustmentSeconds = optInt(SET_TIMER_ADJUSTMENT_SECONDS, 30),
    lastSetRestSeconds = optInt(SET_LAST_SET_REST_SECONDS, 150),
    keepScreenOn = optBoolean(SET_KEEP_SCREEN_ON, false),
    notificationSoundUri = if (isNull(SET_NOTIFICATION_SOUND_URI)) null
    else optString(SET_NOTIFICATION_SOUND_URI),
    rpeTrackingEnabled = optBoolean(SET_RPE_TRACKING_ENABLED, false),
    availableKgPlates = optJSONArray(SET_AVAILABLE_KG_PLATES)?.let { arr ->
        (0 until arr.length()).map { arr.getDouble(it).toFloat() }.toSet()
    } ?: emptySet(),
    availableLbsPlates = optJSONArray(SET_AVAILABLE_LBS_PLATES)?.let { arr ->
        (0 until arr.length()).map { arr.getDouble(it).toFloat() }.toSet()
    } ?: emptySet(),
    oneRmFormula = try {
        OneRmFormula.valueOf(optString(SET_ONE_RM_FORMULA, "EPLEY"))
    } catch (_: Exception) {
        OneRmFormula.EPLEY
    },
    recoveryHoursByMuscle = optJSONObject(SET_RECOVERY_HOURS_BY_MUSCLE)?.let { obj ->
        obj.keys().asSequence().mapNotNull { key ->
            val mg = try { MuscleGroup.valueOf(key) } catch (_: Exception) { null }
            val hrs = obj.optString(key).toIntOrNull()
            if (mg != null && hrs != null) mg to hrs else null
        }.toMap()
    } ?: emptyMap(),
    userSex = try {
        Sex.valueOf(optString(SET_USER_SEX, "UNSET"))
    } catch (_: Exception) {
        Sex.UNSET
    },
    birthYear = optInt(SET_BIRTH_YEAR, 0),
    caliperMode = try {
        CaliperMode.valueOf(optString(SET_CALIPER_MODE, "THREE_SITE"))
    } catch (_: Exception) {
        CaliperMode.THREE_SITE
    }
)

fun ExportData.toJson(): String = JSONObject().apply {
    put(KEY_VERSION, version)
    put(KEY_EXPORTED_AT, exportedAt)
    put(KEY_SOURCE, source)
    put(KEY_EXERCISES, JSONArray(exercises.map { it.toJson() }))
    put(KEY_ROUTINE_GROUPS, JSONArray(routineGroups.map { it.toJson() }))
    put(KEY_ROUTINES, JSONArray(routines.map { it.toJson() }))
    put(KEY_ROUTINE_EXERCISES, JSONArray(routineExercises.map { it.toJson() }))
    put(KEY_ROUTINE_SETS, JSONArray(routineSets.map { it.toJson() }))
    put(KEY_EXERCISE_NOTES, JSONArray(exerciseNotes.map { it.toJson() }))
    put(KEY_WORKOUTS, JSONArray(workouts.map { it.toJson() }))
    put(KEY_WORKOUT_EXERCISES, JSONArray(workoutExercises.map { it.toJson() }))
    put(KEY_SETS, JSONArray(sets.map { it.toJson() }))
    put(KEY_MEASUREMENT_ENTRIES, JSONArray(measurementEntries.map { it.toJson() }))
    put(KEY_SETTINGS, settings.toJson())
}.toString(2)

fun String.parseExportData(): ExportData? = try {
    val root = JSONObject(this)
    ExportData(
        version = root.optInt(KEY_VERSION, 1),
        exportedAt = root.optLong(KEY_EXPORTED_AT, System.currentTimeMillis()),
        source = root.optString(KEY_SOURCE, ""),
        exercises = root.optJSONArray(KEY_EXERCISES)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toExercise() }
        } ?: emptyList(),
        routineGroups = root.optJSONArray(KEY_ROUTINE_GROUPS)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toRoutineGroup() }
        } ?: emptyList(),
        routines = root.optJSONArray(KEY_ROUTINES)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toRoutine() }
        } ?: emptyList(),
        routineExercises = root.optJSONArray(KEY_ROUTINE_EXERCISES)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toRoutineExercise() }
        } ?: emptyList(),
        routineSets = root.optJSONArray(KEY_ROUTINE_SETS)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toRoutineSet() }
        } ?: emptyList(),
        exerciseNotes = root.optJSONArray(KEY_EXERCISE_NOTES)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toExerciseNote() }
        } ?: emptyList(),
        workouts = root.optJSONArray(KEY_WORKOUTS)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toWorkout() }
        } ?: emptyList(),
        workoutExercises = root.optJSONArray(KEY_WORKOUT_EXERCISES)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toWorkoutExercise() }
        } ?: emptyList(),
        sets = root.optJSONArray(KEY_SETS)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toSetLog() }
        } ?: emptyList(),
        measurementEntries = root.optJSONArray(KEY_MEASUREMENT_ENTRIES)?.let { arr ->
            (0 until arr.length()).map { arr.getJSONObject(it).toMeasurementEntry() }
        } ?: emptyList(),
        settings = root.optJSONObject(KEY_SETTINGS)?.toAppSettings() ?: AppSettings()
    )
} catch (e: Exception) {
    null
}
