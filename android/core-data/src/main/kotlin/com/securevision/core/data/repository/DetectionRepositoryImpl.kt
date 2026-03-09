package com.securevision.core.data.repository

import com.securevision.core.data.local.dao.DetectionEventDao
import com.securevision.core.data.local.entity.DetectionEventEntity
import com.securevision.core.domain.model.DetectionEvent
import com.securevision.core.domain.model.DetectionType
import com.securevision.core.domain.repository.DetectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DetectionRepositoryImpl @Inject constructor(
    private val detectionEventDao: DetectionEventDao
) : DetectionRepository {

    override fun getDetectionEvents(): Flow<List<DetectionEvent>> =
        detectionEventDao.getDetectionEvents().map { it.map(DetectionEventEntity::toDomain) }

    override fun getDetectionEventsByType(type: DetectionType): Flow<List<DetectionEvent>> =
        detectionEventDao.getDetectionEventsByType(type.name)
            .map { it.map(DetectionEventEntity::toDomain) }

    override fun getDetectionEventsByCamera(cameraId: String): Flow<List<DetectionEvent>> =
        detectionEventDao.getDetectionEventsByCamera(cameraId)
            .map { it.map(DetectionEventEntity::toDomain) }

    override fun getDetectionEventsInRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<DetectionEvent>> =
        detectionEventDao.getDetectionEventsInRange(startTime, endTime)
            .map { it.map(DetectionEventEntity::toDomain) }

    override suspend fun getDetectionEventById(id: Long): DetectionEvent? =
        detectionEventDao.getDetectionEventById(id)?.toDomain()

    override suspend fun insertDetectionEvent(event: DetectionEvent): Long =
        detectionEventDao.insertDetectionEvent(DetectionEventEntity.fromDomain(event))

    override suspend fun deleteDetectionEvent(id: Long) =
        detectionEventDao.deleteDetectionEvent(id)

    override suspend fun deleteDetectionEventsOlderThan(timestamp: Long) =
        detectionEventDao.deleteDetectionEventsOlderThan(timestamp)

    override fun getDetectionEventCount(): Flow<Int> =
        detectionEventDao.getDetectionEventCount()
}
