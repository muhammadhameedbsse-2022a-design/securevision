package com.securevision.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val alertSoundEnabled: Boolean = true,
    val alertVibrationEnabled: Boolean = true,
    val faceDetectionEnabled: Boolean = true,
    val weaponDetectionEnabled: Boolean = true,
    val attributeDetectionEnabled: Boolean = false,
    val detectionConfidenceThreshold: Float = 0.7f,
    val retentionDays: Int = 30,
    val cameraResolution: CameraResolution = CameraResolution.HD,
    val darkMode: Boolean = true,
    val successMessage: String? = null,
    val error: String? = null
)

enum class CameraResolution(val label: String) {
    SD("Standard (480p)"),
    HD("HD (720p)"),
    FULL_HD("Full HD (1080p)")
}

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        persistSettings()
    }

    fun setAlertSoundEnabled(enabled: Boolean) {
        _uiState.update { it.copy(alertSoundEnabled = enabled) }
        persistSettings()
    }

    fun setAlertVibrationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(alertVibrationEnabled = enabled) }
        persistSettings()
    }

    fun setFaceDetectionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(faceDetectionEnabled = enabled) }
        persistSettings()
    }

    fun setWeaponDetectionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(weaponDetectionEnabled = enabled) }
        persistSettings()
    }

    fun setAttributeDetectionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(attributeDetectionEnabled = enabled) }
        persistSettings()
    }

    fun setConfidenceThreshold(threshold: Float) {
        _uiState.update { it.copy(detectionConfidenceThreshold = threshold) }
    }

    fun setRetentionDays(days: Int) {
        _uiState.update { it.copy(retentionDays = days) }
        persistSettings()
    }

    fun setCameraResolution(resolution: CameraResolution) {
        _uiState.update { it.copy(cameraResolution = resolution) }
        persistSettings()
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(darkMode = enabled) }
        persistSettings()
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun persistSettings() {
        viewModelScope.launch {
            // Persist to DataStore in a real implementation
            _uiState.update { it.copy(successMessage = "Settings saved") }
        }
    }
}
