package com.strongest.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.data.db.MuscleVolume
import com.strongest.app.data.db.PersonalRecord
import com.strongest.app.data.db.VolumeByDate
import com.strongest.app.data.db.WorkoutsPerDay
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.utils.localDayStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil
import javax.inject.Inject

enum class ProgressRange(val days: Int, val label: String) {
    DAYS_7(7, "7d"),
    DAYS_30(30, "30d"),
    DAYS_90(90, "90d"),
    DAYS_182(182, "6m"),
    DAYS_365(365, "1y")
}

enum class ProgressMetric(val label: String) {
    SETS("Sets"),
    WEIGHT("Weight"),
    WORKOUTS("Workouts")
}

/** One recovering primary muscle: hours left until recovered and how far along it is (0..1). */
data class MuscleRecovery(
    val muscleGroup: MuscleGroup,
    val hoursRemaining: Int,
    val fractionRecovered: Float
)

data class ProgressUiState(
    val isLoading: Boolean = false,
    val range: ProgressRange = ProgressRange.DAYS_30,
    val metric: ProgressMetric = ProgressMetric.SETS,
    val personalRecords: List<PersonalRecord> = emptyList(),
    val volumeByDay: List<VolumeByDate> = emptyList(),
    val workoutsPerDay: List<WorkoutsPerDay> = emptyList(),
    val muscleVolume: List<MuscleVolume> = emptyList(),
    val recoveringMuscles: List<MuscleRecovery> = emptyList(),
    val prMuscleFilter: MuscleGroup? = null,
    val prEquipmentFilter: Equipment? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    val weightUnit: StateFlow<WeightUnit> = settingsRepository.settingsFlow
        .map { it.weightUnit }
        .stateIn(viewModelScope, SharingStarted.Lazily, WeightUnit.KG)

    /** Which body to draw on the heatmap; unset profiles get the male figure. */
    val bodyFigure: StateFlow<BodyFigure> = settingsRepository.settingsFlow
        .map { it.userSex.toBodyFigure() }
        .stateIn(viewModelScope, SharingStarted.Lazily, BodyFigure.MALE)

    init {
        loadAll()
    }

    fun setRange(range: ProgressRange) {
        if (range == _state.value.range) return
        _state.update { it.copy(range = range) }
        loadRanged()
    }

    fun setMetric(metric: ProgressMetric) {
        if (metric == _state.value.metric) return
        _state.update { it.copy(metric = metric) }
    }

    fun setPrMuscleFilter(group: MuscleGroup?) {
        _state.update { it.copy(prMuscleFilter = group) }
    }

    fun setPrEquipmentFilter(equipment: Equipment?) {
        _state.update { it.copy(prEquipmentFilter = equipment) }
    }

    fun refresh() {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val prs = repository.getAllPersonalRecords()
            _state.update { it.copy(personalRecords = prs) }
            loadRecovery()
            loadRanged()
        }
    }

    /** Recovery is independent of the selected range — it's based on when each muscle was last trained. */
    private suspend fun loadRecovery() {
        val recoveryByMuscle = settingsRepository.settingsFlow.first().recoveryHoursByMuscle
        val lastTrained = repository.getMuscleLastTrained()
        val now = System.currentTimeMillis()
        val recovering = lastTrained.mapNotNull { row ->
            val mg = runCatching { MuscleGroup.valueOf(row.muscleGroup) }.getOrNull()
                ?: return@mapNotNull null
            // Only muscle groups that have a configured recovery window are tracked.
            val hours = recoveryByMuscle[mg] ?: return@mapNotNull null
            val windowMs = hours * 3600_000L
            val elapsed = now - row.lastTrained
            if (elapsed < 0 || elapsed >= windowMs) return@mapNotNull null
            MuscleRecovery(
                muscleGroup = mg,
                hoursRemaining = ceil((windowMs - elapsed) / 3600_000.0).toInt(),
                fractionRecovered = (elapsed.toFloat() / windowMs).coerceIn(0f, 1f)
            )
        }.sortedByDescending { it.hoursRemaining } // least recovered first
        _state.update { it.copy(recoveringMuscles = recovering) }
    }

    private fun loadRanged() {
        viewModelScope.launch {
            val rangeDays = _state.value.range.days
            // Align the query to the same calendar-day boundaries the chart uses. Snap the result
            // back to local midnight: fixed 24h steps drift by 1h across DST transitions.
            val startDate = localDayStart(
                localDayStart(System.currentTimeMillis()) - (rangeDays - 1) * 24L * 60 * 60 * 1000
            )
            val volume = repository.getVolumeByDate(startDate)
            val muscle = repository.getMuscleVolume(startDate)
            val perDay = repository.getWorkoutsPerDay(startDate)
            // Aggregate volume/sets per local calendar day so the chart can sit on a continuous
            // day axis (multiple workouts on one day collapse into a single point).
            val volumeByDay = volume
                .groupBy { localDayStart(it.date) }
                .map { (day, rows) ->
                    VolumeByDate(
                        date = day,
                        totalVolumeKg = rows.sumOf { it.totalVolumeKg.toDouble() }.toFloat(),
                        totalSets = rows.sumOf { it.totalSets }
                    )
                }
                .sortedBy { it.date }
            _state.update {
                it.copy(
                    isLoading = false,
                    volumeByDay = volumeByDay,
                    muscleVolume = muscle,
                    workoutsPerDay = perDay
                )
            }
        }
    }
}
