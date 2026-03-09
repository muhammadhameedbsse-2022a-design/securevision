package com.securevision.core.domain.repository

import com.securevision.core.domain.model.Alert
import com.securevision.core.domain.model.AlertSeverity
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun getAlerts(): Flow<List<Alert>>
    fun getUnreadAlerts(): Flow<List<Alert>>
    fun getAlertsBySeverity(severity: AlertSeverity): Flow<List<Alert>>
    suspend fun getAlertById(id: Long): Alert?
    suspend fun insertAlert(alert: Alert): Long
    suspend fun markAlertAsRead(id: Long)
    suspend fun markAllAlertsAsRead()
    suspend fun deleteAlert(id: Long)
    suspend fun deleteAllAlerts()
    fun getUnreadAlertCount(): Flow<Int>
}
