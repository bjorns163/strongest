package com.strongest.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.strongest.app.ThemeMode
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.ui.workout.STANDARD_KG_PLATES
import com.strongest.app.ui.workout.STANDARD_LBS_PLATES
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val defaultRestSeconds: Int = 90,
    val timerAdjustmentSeconds: Int = 30,
    val lastSetRestSeconds: Int = 150,
    val keepScreenOn: Boolean = false,
    val notificationSoundUri: String? = null,
    val rpeTrackingEnabled: Boolean = false,
    val workoutNotificationEnabled: Boolean = true,
    val availableKgPlates: Set<Float> = STANDARD_KG_PLATES.toSet(),
    val availableLbsPlates: Set<Float> = STANDARD_LBS_PLATES.toSet(),
    val oneRmFormula: OneRmFormula = OneRmFormula.EPLEY,
    val recoveryHoursByMuscle: Map<MuscleGroup, Int> = defaultRecoveryHoursMap(),
    val userSex: Sex = Sex.UNSET,
    val birthYear: Int = 0,
    val caliperMode: CaliperMode = CaliperMode.THREE_SITE
)

enum class WeightUnit {
    KG, LBS
}

enum class OneRmFormula {
    EPLEY, BRZYCKI, BOTH
}

enum class Sex {
    MALE, FEMALE, UNSET
}

enum class CaliperMode(val siteCount: Int, val label: String) {
    THREE_SITE(3, "3-site"),
    FOUR_SITE(4, "4-site"),
    SEVEN_SITE(7, "7-site")
}

/** Muscle groups that have a recovery window (trainable, non-cardio/stretching). */
val RECOVERABLE_MUSCLE_GROUPS: List<MuscleGroup> = listOf(
    MuscleGroup.CHEST, MuscleGroup.BACK, MuscleGroup.SHOULDERS, MuscleGroup.BICEPS,
    MuscleGroup.TRICEPS, MuscleGroup.ABS, MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS,
    MuscleGroup.GLUTES, MuscleGroup.CALVES, MuscleGroup.FOREARMS, MuscleGroup.LOWER_BACK,
    MuscleGroup.TRAPS
)

/** Default recovery window per muscle: big groups recover slower, abs faster. */
fun defaultRecoveryHours(muscle: MuscleGroup): Int = when (muscle) {
    MuscleGroup.ABS -> 24
    MuscleGroup.BACK, MuscleGroup.LOWER_BACK,
    MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES -> 72
    else -> 48
}

fun defaultRecoveryHoursMap(): Map<MuscleGroup, Int> =
    RECOVERABLE_MUSCLE_GROUPS.associateWith { defaultRecoveryHours(it) }

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("theme_mode")
    private val WEIGHT_UNIT_KEY = stringPreferencesKey("weight_unit")
    private val DEFAULT_REST_KEY = intPreferencesKey("default_rest_seconds")
    private val TIMER_ADJUST_KEY = intPreferencesKey("timer_adjustment_seconds")
    private val LAST_SET_REST_KEY = intPreferencesKey("last_set_rest_seconds")
    private val KEEP_SCREEN_ON_KEY = booleanPreferencesKey("keep_screen_on")
    private val NOTIFICATION_SOUND_URI_KEY = stringPreferencesKey("notification_sound_uri")
    private val RPE_TRACKING_ENABLED_KEY = booleanPreferencesKey("rpe_tracking_enabled")
    private val WORKOUT_NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("workout_notification_enabled")
    private val AVAILABLE_KG_PLATES_KEY = stringSetPreferencesKey("available_kg_plates")
    private val AVAILABLE_LBS_PLATES_KEY = stringSetPreferencesKey("available_lbs_plates")
    private val ONE_RM_FORMULA_KEY = stringPreferencesKey("one_rm_formula")
    private val EXERCISE_CLASSIFICATION_BACKFILL_DONE_KEY =
        booleanPreferencesKey("exercise_classification_backfill_done")
    private val EXERCISE_SEED_VERSION_KEY = intPreferencesKey("exercise_seed_version")
    private val RECOVERY_BY_MUSCLE_KEY = stringPreferencesKey("recovery_hours_by_muscle")
    private val USER_SEX_KEY = stringPreferencesKey("user_sex")
    private val BIRTH_YEAR_KEY = intPreferencesKey("birth_year")
    private val CALIPER_MODE_KEY = stringPreferencesKey("caliper_mode")

    private fun parsePlateSet(stored: Set<String>?, fallback: Set<Float>): Set<Float> {
        if (stored == null) return fallback
        return stored.mapNotNull { it.toFloatOrNull() }.toSet()
    }

    /** Parses stored "MUSCLE:hours;..." overrides, merged onto the per-muscle defaults. */
    private fun parseRecoveryHours(stored: String?): Map<MuscleGroup, Int> {
        val map = defaultRecoveryHoursMap().toMutableMap()
        if (stored.isNullOrBlank()) return map
        stored.split(";").forEach { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) {
                val mg = runCatching { MuscleGroup.valueOf(parts[0]) }.getOrNull()
                val hrs = parts[1].toIntOrNull()
                if (mg != null && hrs != null) map[mg] = hrs
            }
        }
        return map
    }

    val themeFlow: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            try {
                ThemeMode.valueOf(preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

    val weightUnitFlow: Flow<WeightUnit> = context.dataStore.data
        .map { preferences ->
            try {
                WeightUnit.valueOf(preferences[WEIGHT_UNIT_KEY] ?: WeightUnit.KG.name)
            } catch (e: Exception) {
                WeightUnit.KG
            }
        }

    private inline fun <reified T : Enum<T>> safeEnum(value: String?, default: T): T =
        if (value == null) default
        else try { enumValueOf<T>(value) } catch (_: IllegalArgumentException) { default }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .map { preferences ->
            AppSettings(
                themeMode = safeEnum(preferences[THEME_KEY], ThemeMode.SYSTEM),
                weightUnit = safeEnum(preferences[WEIGHT_UNIT_KEY], WeightUnit.KG),
                defaultRestSeconds = preferences[DEFAULT_REST_KEY] ?: 90,
                timerAdjustmentSeconds = preferences[TIMER_ADJUST_KEY] ?: 30,
                lastSetRestSeconds = preferences[LAST_SET_REST_KEY] ?: 150,
                keepScreenOn = preferences[KEEP_SCREEN_ON_KEY] ?: false,
                notificationSoundUri = preferences[NOTIFICATION_SOUND_URI_KEY],
                rpeTrackingEnabled = preferences[RPE_TRACKING_ENABLED_KEY] ?: false,
                workoutNotificationEnabled = preferences[WORKOUT_NOTIFICATION_ENABLED_KEY] ?: true,
                availableKgPlates = parsePlateSet(preferences[AVAILABLE_KG_PLATES_KEY], STANDARD_KG_PLATES.toSet()),
                availableLbsPlates = parsePlateSet(preferences[AVAILABLE_LBS_PLATES_KEY], STANDARD_LBS_PLATES.toSet()),
                oneRmFormula = safeEnum(preferences[ONE_RM_FORMULA_KEY], OneRmFormula.EPLEY),
                recoveryHoursByMuscle = parseRecoveryHours(preferences[RECOVERY_BY_MUSCLE_KEY]),
                userSex = safeEnum(preferences[USER_SEX_KEY], Sex.UNSET),
                birthYear = preferences[BIRTH_YEAR_KEY] ?: 0,
                caliperMode = safeEnum(preferences[CALIPER_MODE_KEY], CaliperMode.THREE_SITE)
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { preferences ->
            preferences[WEIGHT_UNIT_KEY] = unit.name
        }
    }

    suspend fun setDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_REST_KEY] = seconds
        }
    }

    suspend fun setTimerAdjustmentSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[TIMER_ADJUST_KEY] = seconds
        }
    }

    suspend fun setLastSetRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SET_REST_KEY] = seconds
        }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEEP_SCREEN_ON_KEY] = enabled
        }
    }

    suspend fun setRpeTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RPE_TRACKING_ENABLED_KEY] = enabled
        }
    }

    suspend fun setWorkoutNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WORKOUT_NOTIFICATION_ENABLED_KEY] = enabled
        }
    }

    suspend fun setAvailablePlates(unit: WeightUnit, plates: Set<Float>) {
        context.dataStore.edit { preferences ->
            val key = if (unit == WeightUnit.KG) AVAILABLE_KG_PLATES_KEY else AVAILABLE_LBS_PLATES_KEY
            preferences[key] = plates.map { it.toString() }.toSet()
        }
    }

    suspend fun setOneRmFormula(formula: OneRmFormula) {
        context.dataStore.edit { preferences ->
            preferences[ONE_RM_FORMULA_KEY] = formula.name
        }
    }

    suspend fun setRecoveryHoursForMuscle(muscle: MuscleGroup, hours: Int) {
        context.dataStore.edit { preferences ->
            val map = parseRecoveryHours(preferences[RECOVERY_BY_MUSCLE_KEY]).toMutableMap()
            map[muscle] = hours
            preferences[RECOVERY_BY_MUSCLE_KEY] =
                map.entries.joinToString(";") { "${it.key.name}:${it.value}" }
        }
    }

    suspend fun setUserSex(sex: Sex) {
        context.dataStore.edit { preferences ->
            preferences[USER_SEX_KEY] = sex.name
        }
    }

    suspend fun setBirthYear(year: Int) {
        context.dataStore.edit { preferences ->
            preferences[BIRTH_YEAR_KEY] = year
        }
    }

    suspend fun setCaliperMode(mode: CaliperMode) {
        context.dataStore.edit { preferences ->
            preferences[CALIPER_MODE_KEY] = mode.name
        }
    }

    suspend fun isExerciseClassificationBackfillDone(): Boolean {
        return context.dataStore.data
            .map { it[EXERCISE_CLASSIFICATION_BACKFILL_DONE_KEY] ?: false }
            .first()
    }

    suspend fun markExerciseClassificationBackfillDone() {
        context.dataStore.edit { preferences ->
            preferences[EXERCISE_CLASSIFICATION_BACKFILL_DONE_KEY] = true
        }
    }

    suspend fun getExerciseSeedVersion(): Int {
        return context.dataStore.data
            .map { it[EXERCISE_SEED_VERSION_KEY] ?: 0 }
            .first()
    }

    suspend fun setExerciseSeedVersion(version: Int) {
        context.dataStore.edit { preferences ->
            preferences[EXERCISE_SEED_VERSION_KEY] = version
        }
    }

    suspend fun setNotificationSoundUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[NOTIFICATION_SOUND_URI_KEY] = uri
            } else {
                preferences.remove(NOTIFICATION_SOUND_URI_KEY)
            }
        }
    }

    suspend fun importSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = settings.themeMode.name
            preferences[WEIGHT_UNIT_KEY] = settings.weightUnit.name
            preferences[DEFAULT_REST_KEY] = settings.defaultRestSeconds
            preferences[TIMER_ADJUST_KEY] = settings.timerAdjustmentSeconds
            preferences[LAST_SET_REST_KEY] = settings.lastSetRestSeconds
            preferences[KEEP_SCREEN_ON_KEY] = settings.keepScreenOn
            preferences[RPE_TRACKING_ENABLED_KEY] = settings.rpeTrackingEnabled
            preferences[WORKOUT_NOTIFICATION_ENABLED_KEY] = settings.workoutNotificationEnabled
            preferences[AVAILABLE_KG_PLATES_KEY] = settings.availableKgPlates.map { it.toString() }.toSet()
            preferences[AVAILABLE_LBS_PLATES_KEY] = settings.availableLbsPlates.map { it.toString() }.toSet()
            preferences[ONE_RM_FORMULA_KEY] = settings.oneRmFormula.name
            preferences[RECOVERY_BY_MUSCLE_KEY] =
                settings.recoveryHoursByMuscle.entries.joinToString(";") { "${it.key.name}:${it.value}" }
            preferences[USER_SEX_KEY] = settings.userSex.name
            preferences[BIRTH_YEAR_KEY] = settings.birthYear
            preferences[CALIPER_MODE_KEY] = settings.caliperMode.name
            if (settings.notificationSoundUri != null) {
                preferences[NOTIFICATION_SOUND_URI_KEY] = settings.notificationSoundUri
            } else {
                preferences.remove(NOTIFICATION_SOUND_URI_KEY)
            }
        }
    }
}
