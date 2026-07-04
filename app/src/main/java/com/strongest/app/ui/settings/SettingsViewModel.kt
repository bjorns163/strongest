package com.strongest.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.ThemeMode
import com.strongest.app.data.db.ExerciseDao
import com.strongest.app.data.db.MeasurementEntryDao
import com.strongest.app.data.db.RoutineDao
import com.strongest.app.data.db.StrongestDatabase
import com.strongest.app.data.db.WorkoutDao
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.MeasurementEntry
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SetLog
import com.strongest.app.data.model.Workout
import com.strongest.app.data.model.WorkoutExercise
import com.strongest.app.data.repository.CaliperMode
import com.strongest.app.data.repository.OneRmFormula
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.Sex
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.data.repository.defaultRecoveryHoursMap
import com.strongest.app.utils.ExportData
import com.strongest.app.utils.parseExportData
import com.strongest.app.utils.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val defaultRestSeconds: Int = 90,
    val timerAdjustmentSeconds: Int = 30,
    val lastSetRestSeconds: Int = 150,
    val keepScreenOn: Boolean = false,
    val notificationSoundUri: String? = null,
    val rpeTrackingEnabled: Boolean = false,
    val workoutNotificationEnabled: Boolean = true,
    val availableKgPlates: Map<Float, Int> = emptyMap(),
    val availableLbsPlates: Map<Float, Int> = emptyMap(),
    val oneRmFormula: OneRmFormula = OneRmFormula.EPLEY,
    val recoveryHoursByMuscle: Map<MuscleGroup, Int> = defaultRecoveryHoursMap(),
    val userSex: Sex = Sex.UNSET,
    val birthYear: Int = 0,
    val caliperMode: CaliperMode = CaliperMode.THREE_SITE
)

sealed interface ExportImportResult {
    data object Idle : ExportImportResult
    data object InProgress : ExportImportResult
    data object ExportSuccess : ExportImportResult
    data class ImportSuccess(val workoutCount: Int, val exerciseCount: Int) : ExportImportResult
    data class Error(val message: String) : ExportImportResult
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val database: StrongestDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _exportImportResult = MutableStateFlow<ExportImportResult>(ExportImportResult.Idle)
    val exportImportResult: StateFlow<ExportImportResult> = _exportImportResult.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { appSettings ->
                    _uiState.update {
                        it.copy(
                            themeMode = appSettings.themeMode,
                            weightUnit = appSettings.weightUnit,
                            defaultRestSeconds = appSettings.defaultRestSeconds,
                            timerAdjustmentSeconds = appSettings.timerAdjustmentSeconds,
                            lastSetRestSeconds = appSettings.lastSetRestSeconds,
                            keepScreenOn = appSettings.keepScreenOn,
                            notificationSoundUri = appSettings.notificationSoundUri,
                            rpeTrackingEnabled = appSettings.rpeTrackingEnabled,
                            workoutNotificationEnabled = appSettings.workoutNotificationEnabled,
                            availableKgPlates = appSettings.availableKgPlates,
                            availableLbsPlates = appSettings.availableLbsPlates,
                            oneRmFormula = appSettings.oneRmFormula,
                            recoveryHoursByMuscle = appSettings.recoveryHoursByMuscle,
                            userSex = appSettings.userSex,
                            birthYear = appSettings.birthYear,
                            caliperMode = appSettings.caliperMode
                        )
                    }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            settingsRepository.setWeightUnit(unit)
        }
    }

    fun setDefaultRestSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultRestSeconds(seconds)
        }
    }

    fun setTimerAdjustmentSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setTimerAdjustmentSeconds(seconds)
        }
    }

    fun setLastSetRestSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setLastSetRestSeconds(seconds)
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setKeepScreenOn(enabled)
        }
    }

    fun setNotificationSoundUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setNotificationSoundUri(uri)
        }
    }

    fun setRpeTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRpeTrackingEnabled(enabled)
        }
    }

    fun setWorkoutNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWorkoutNotificationEnabled(enabled)
        }
    }

    fun setAvailablePlates(unit: WeightUnit, plates: Map<Float, Int>) {
        viewModelScope.launch {
            settingsRepository.setAvailablePlates(unit, plates)
        }
    }

    fun setOneRmFormula(formula: OneRmFormula) {
        viewModelScope.launch {
            settingsRepository.setOneRmFormula(formula)
        }
    }

    fun setRecoveryHoursForMuscle(muscle: MuscleGroup, hours: Int) {
        viewModelScope.launch {
            settingsRepository.setRecoveryHoursForMuscle(muscle, hours)
        }
    }

    fun setUserSex(sex: Sex) {
        viewModelScope.launch {
            settingsRepository.setUserSex(sex)
        }
    }

    fun setBirthYear(year: Int) {
        viewModelScope.launch {
            settingsRepository.setBirthYear(year)
        }
    }

    fun setCaliperMode(mode: CaliperMode) {
        viewModelScope.launch {
            settingsRepository.setCaliperMode(mode)
        }
    }

    fun resetExportImportResult() {
        _exportImportResult.value = ExportImportResult.Idle
    }

    fun exportData(context: Context, outputUri: Uri) {
        viewModelScope.launch {
            _exportImportResult.value = ExportImportResult.InProgress
            try {
                val exportData = withContext(Dispatchers.IO) { collectExportData() }
                val json = exportData.toJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(outputUri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    } ?: throw Exception("Could not open output file")
                }
                _exportImportResult.value = ExportImportResult.ExportSuccess
            } catch (e: Exception) {
                _exportImportResult.value = ExportImportResult.Error(
                    "Export failed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun importData(context: Context, inputUri: Uri) {
        viewModelScope.launch {
            _exportImportResult.value = ExportImportResult.InProgress
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(inputUri)?.use { stream ->
                        stream.bufferedReader(Charsets.UTF_8).readText()
                    } ?: throw Exception("Could not open input file")
                }
                val exportData = json.parseExportData()
                    ?: throw Exception("Invalid or corrupted export file")

                withContext(Dispatchers.IO) {
                    importExportData(exportData)
                }

                _exportImportResult.value = ExportImportResult.ImportSuccess(
                    workoutCount = exportData.workouts.size,
                    exerciseCount = exportData.exercises.size
                )
            } catch (e: Exception) {
                _exportImportResult.value = ExportImportResult.Error(
                    "Import failed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    private suspend fun collectExportData(): ExportData {
        val dao = database
        val exerciseDao = dao.exerciseDao()
        val routineDao = dao.routineDao()
        val workoutDao = dao.workoutDao()
        val measurementDao = dao.measurementEntryDao()

        return ExportData(
            exercises = exerciseDao.getAllExercisesList(),
            routineGroups = routineDao.getAllRoutineGroupsList(),
            routines = routineDao.getAllRoutinesList(),
            routineExercises = routineDao.getAllRoutineExercisesList(),
            routineSets = routineDao.getAllRoutineSetsList(),
            exerciseNotes = exerciseDao.getAllNotes(),
            workouts = workoutDao.getAllWorkoutsList(),
            workoutExercises = workoutDao.getAllWorkoutExercises(),
            sets = workoutDao.getAllSets(),
            measurementEntries = measurementDao.getAllEntriesList(),
            settings = settingsRepository.settingsFlow.first()
        )
    }

    private suspend fun importExportData(data: ExportData) {
        val db = database.openHelper.writableDatabase
        val exerciseDao = database.exerciseDao()
        val routineDao = database.routineDao()
        val workoutDao = database.workoutDao()
        val measurementDao = database.measurementEntryDao()

        db.execSQL("PRAGMA foreign_keys = OFF")
        try {
            db.execSQL("DELETE FROM exercise_notes")
            db.execSQL("DELETE FROM measurement_entries")
            db.execSQL("DELETE FROM routine_sets")
            db.execSQL("DELETE FROM routine_exercises")
            db.execSQL("DELETE FROM routines")
            db.execSQL("DELETE FROM routine_groups")
            db.execSQL("DELETE FROM sets")
            db.execSQL("DELETE FROM workout_exercises")
            db.execSQL("DELETE FROM workouts")
            db.execSQL("DELETE FROM exercises")

            for (exercise in data.exercises) {
                exerciseDao.insertExercise(exercise)
            }
            for (group in data.routineGroups) {
                routineDao.insertRoutineGroup(group)
            }
            for (routine in data.routines) {
                routineDao.insertRoutine(routine)
            }
            if (data.routineExercises.isNotEmpty()) {
                routineDao.insertRoutineExercises(data.routineExercises)
            }
            if (data.routineSets.isNotEmpty()) {
                routineDao.insertRoutineSets(data.routineSets)
            }
            for (note in data.exerciseNotes) {
                exerciseDao.upsertNote(note)
            }
            for (workout in data.workouts) {
                workoutDao.insertWorkout(workout)
            }
            if (data.workoutExercises.isNotEmpty()) {
                workoutDao.insertWorkoutExercises(data.workoutExercises)
            }
            if (data.sets.isNotEmpty()) {
                workoutDao.insertSets(data.sets)
            }
            for (entry in data.measurementEntries) {
                measurementDao.insertEntry(entry)
            }
            settingsRepository.importSettings(data.settings)
        } finally {
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }
}
