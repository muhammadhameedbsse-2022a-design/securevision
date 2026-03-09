package com.securevision.feature.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevision.core.domain.model.Alert
import com.securevision.core.domain.model.AlertSeverity
import com.securevision.core.domain.repository.AlertRepository
import com.securevision.core.domain.usecase.GetAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val isLoading: Boolean = true,
    val alerts: List<Alert> = emptyList(),
    val filteredAlerts: List<Alert> = emptyList(),
    val selectedSeverity: AlertSeverity? = null,
    val showUnreadOnly: Boolean = false,
    val unreadCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            combine(
                getAlertsUseCase(),
                getAlertsUseCase.getUnreadCount()
            ) { alerts, unreadCount ->
                Pair(alerts, unreadCount)
            }.collect { (alerts, unreadCount) ->
                _uiState.update { state ->
                    val filtered = applyFilters(alerts, state.selectedSeverity, state.showUnreadOnly)
                    state.copy(
                        isLoading = false,
                        alerts = alerts,
                        filteredAlerts = filtered,
                        unreadCount = unreadCount
                    )
                }
            }
        }
    }

    fun filterBySeverity(severity: AlertSeverity?) {
        _uiState.update { state ->
            val filtered = applyFilters(state.alerts, severity, state.showUnreadOnly)
            state.copy(selectedSeverity = severity, filteredAlerts = filtered)
        }
    }

    fun toggleUnreadFilter() {
        _uiState.update { state ->
            val newFilter = !state.showUnreadOnly
            val filtered = applyFilters(state.alerts, state.selectedSeverity, newFilter)
            state.copy(showUnreadOnly = newFilter, filteredAlerts = filtered)
        }
    }

    fun markAsRead(alertId: Long) {
        viewModelScope.launch {
            alertRepository.markAlertAsRead(alertId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            alertRepository.markAllAlertsAsRead()
        }
    }

    fun deleteAlert(alertId: Long) {
        viewModelScope.launch {
            alertRepository.deleteAlert(alertId)
        }
    }

    private fun applyFilters(
        alerts: List<Alert>,
        severity: AlertSeverity?,
        unreadOnly: Boolean
    ): List<Alert> = alerts.filter { alert ->
        val matchesSeverity = severity == null || alert.severity == severity
        val matchesRead = !unreadOnly || !alert.isRead
        matchesSeverity && matchesRead
    }
}
