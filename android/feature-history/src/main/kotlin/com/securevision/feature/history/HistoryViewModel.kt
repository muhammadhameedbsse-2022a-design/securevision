package com.securevision.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevision.core.domain.model.DetectionEvent
import com.securevision.core.domain.model.DetectionType
import com.securevision.core.domain.usecase.GetDetectionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val events: List<DetectionEvent> = emptyList(),
    val filteredEvents: List<DetectionEvent> = emptyList(),
    val selectedType: DetectionType? = null,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getDetectionHistoryUseCase: GetDetectionHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getDetectionHistoryUseCase().collect { events ->
                _uiState.update { state ->
                    val filtered = if (state.selectedType != null) {
                        events.filter { it.detectionType == state.selectedType }
                    } else events
                    state.copy(isLoading = false, events = events, filteredEvents = filtered)
                }
            }
        }
    }

    fun filterByType(type: DetectionType?) {
        _uiState.update { state ->
            val filtered = if (type != null) {
                state.events.filter { it.detectionType == type }
            } else state.events
            state.copy(selectedType = type, filteredEvents = filtered)
        }
    }

    fun clearFilter() = filterByType(null)
}
