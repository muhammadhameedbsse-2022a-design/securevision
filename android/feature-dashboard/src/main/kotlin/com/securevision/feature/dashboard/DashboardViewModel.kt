package com.securevision.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevision.core.domain.model.Alert
import com.securevision.core.domain.model.DetectionEvent
import com.securevision.core.domain.usecase.GetAlertsUseCase
import com.securevision.core.domain.usecase.GetDetectionHistoryUseCase
import com.securevision.core.domain.usecase.GetProfilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val recentAlerts: List<Alert> = emptyList(),
    val recentEvents: List<DetectionEvent> = emptyList(),
    val totalProfiles: Int = 0,
    val unreadAlertCount: Int = 0,
    val totalDetections: Int = 0,
    val activeCamera: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    private val getDetectionHistoryUseCase: GetDetectionHistoryUseCase,
    private val getProfilesUseCase: GetProfilesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                getAlertsUseCase(),
                getDetectionHistoryUseCase(),
                getProfilesUseCase(),
                getAlertsUseCase.getUnreadCount()
            ) { alerts, events, profiles, unreadCount ->
                DashboardUiState(
                    isLoading = false,
                    recentAlerts = alerts.take(5),
                    recentEvents = events.take(10),
                    totalProfiles = profiles.size,
                    unreadAlertCount = unreadCount,
                    totalDetections = events.size,
                    activeCamera = false,
                    error = null
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun toggleCamera() {
        _uiState.update { it.copy(activeCamera = !it.activeCamera) }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadDashboardData()
    }
}
