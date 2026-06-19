package com.strongest.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.ThemeMode
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.CaliperMode
import com.strongest.app.data.repository.OneRmFormula
import com.strongest.app.data.repository.Sex
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.data.repository.defaultRecoveryHoursMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val availableKgPlates: Set<Float> = emptySet(),
    val availableLbsPlates: Set<Float> = emptySet(),
    val oneRmFormula: OneRmFormula = OneRmFormula.EPLEY,
    val recoveryHoursByMuscle: Map<MuscleGroup, Int> = defaultRecoveryHoursMap(),
    val userSex: Sex = Sex.UNSET,
    val birthYear: Int = 0,
    val caliperMode: CaliperMode = CaliperMode.THREE_SITE
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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

    fun setAvailablePlates(unit: WeightUnit, plates: Set<Float>) {
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
}
