package com.securevision.core.data.repository

import com.securevision.core.data.local.dao.AlertDao
import com.securevision.core.data.local.entity.AlertEntity
import com.securevision.core.domain.model.Alert
import com.securevision.core.domain.model.AlertSeverity
import com.securevision.core.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlertRepositoryImpl @Inject constructor(
    private val alertDao: AlertDao
) : AlertRepository {

    override fun getAlerts(): Flow<List<Alert>> =
        alertDao.getAlerts().map { entities -> entities.map { it.toDomain() } }

    override fun getUnreadAlerts(): Flow<List<Alert>> =
        alertDao.getUnreadAlerts().map { entities -> entities.map { it.toDomain() } }

    override fun getAlertsBySeverity(severity: AlertSeverity): Flow<List<Alert>> =
        alertDao.getAlertsBySeverity(severity.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAlertById(id: Long): Alert? =
        alertDao.getAlertById(id)?.toDomain()

    override suspend fun insertAlert(alert: Alert): Long =
        alertDao.insertAlert(AlertEntity.fromDomain(alert))

    override suspend fun markAlertAsRead(id: Long) =
        alertDao.markAlertAsRead(id)

    override suspend fun markAllAlertsAsRead() =
        alertDao.markAllAlertsAsRead()

    override suspend fun deleteAlert(id: Long) =
        alertDao.deleteAlert(id)

    override suspend fun deleteAllAlerts() =
        alertDao.deleteAllAlerts()

    override fun getUnreadAlertCount(): Flow<Int> =
        alertDao.getUnreadAlertCount()
}
