package com.securevision.core.domain.usecase

import com.securevision.core.domain.model.Alert
import com.securevision.core.domain.model.AlertSeverity
import com.securevision.core.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlertsUseCase @Inject constructor(private val alertRepository: AlertRepository) {

    /** Returns all alerts ordered by timestamp descending. */
    operator fun invoke(): Flow<List<Alert>> = alertRepository.getAlerts()

    /** Returns only unread alerts. */
    fun getUnread(): Flow<List<Alert>> = alertRepository.getUnreadAlerts()

    /** Returns alerts filtered by severity level. */
    fun getBySeverity(severity: AlertSeverity): Flow<List<Alert>> =
        alertRepository.getAlertsBySeverity(severity)

    /** Returns the current count of unread alerts. */
    fun getUnreadCount(): Flow<Int> = alertRepository.getUnreadAlertCount()
}
