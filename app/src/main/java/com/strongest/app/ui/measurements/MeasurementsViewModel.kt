package com.strongest.app.ui.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.data.db.LatestMeasurement
import com.strongest.app.data.model.BodyMetric
import com.strongest.app.data.model.MeasurementEntry
import com.strongest.app.data.repository.CaliperMode
import com.strongest.app.data.repository.MeasurementRepository
import com.strongest.app.data.repository.Sex
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeasurementsListViewModel @Inject constructor(
    repository: MeasurementRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val latestByMetric: StateFlow<Map<BodyMetric, LatestMeasurement>> =
        repository.getLatestPerMetric()
            .map { list ->
                list.mapNotNull { lm ->
                    runCatching { BodyMetric.valueOf(lm.metric) }.getOrNull()?.let { it to lm }
                }.toMap()
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    val weightUnit: StateFlow<WeightUnit> = settingsRepository.settingsFlow
        .map { it.weightUnit }
        .stateIn(viewModelScope, SharingStarted.Lazily, WeightUnit.KG)
}

/** Profile + protocol the guided caliper body-fat flow needs. */
data class CaliperProfile(
    val sex: Sex = Sex.UNSET,
    val birthYear: Int = 0,
    val mode: CaliperMode = CaliperMode.THREE_SITE
)

@HiltViewModel
class MeasurementDetailViewModel @Inject constructor(
    private val repository: MeasurementRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var currentMetric: BodyMetric = BodyMetric.WEIGHT
    private var loadJob: Job? = null

    val weightUnit: StateFlow<WeightUnit> = settingsRepository.settingsFlow
        .map { it.weightUnit }
        .stateIn(viewModelScope, SharingStarted.Lazily, WeightUnit.KG)

    val caliperProfile: StateFlow<CaliperProfile> = settingsRepository.settingsFlow
        .map { CaliperProfile(it.userSex, it.birthYear, it.caliperMode) }
        .stateIn(viewModelScope, SharingStarted.Lazily, CaliperProfile())

    fun saveProfile(sex: Sex, birthYear: Int) {
        viewModelScope.launch {
            settingsRepository.setUserSex(sex)
            settingsRepository.setBirthYear(birthYear)
        }
    }

    private val _entries = kotlinx.coroutines.flow.MutableStateFlow<List<MeasurementEntry>>(emptyList())
    val entries: StateFlow<List<MeasurementEntry>> = _entries

    fun load(metric: BodyMetric) {
        currentMetric = metric
        // Cancel any in-flight collector from a previous metric so they don't race writing _entries.
        loadJob?.cancel()
        _entries.value = emptyList()
        loadJob = viewModelScope.launch {
            repository.getEntriesForMetric(metric).collect { _entries.value = it }
        }
    }

    fun addEntry(value: Float, timestamp: Long, notes: String = "") {
        viewModelScope.launch {
            repository.addEntry(currentMetric, value, timestamp, notes)
        }
    }

    fun deleteEntry(entry: MeasurementEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }
}
