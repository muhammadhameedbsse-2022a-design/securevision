package com.securevision.core.domain.usecase

import com.securevision.core.domain.model.DetectionEvent
import com.securevision.core.domain.model.DetectionType
import com.securevision.core.domain.repository.DetectionRepository
import kotlinx.coroutines.flow.Flow

class GetDetectionHistoryUseCase(private val detectionRepository: DetectionRepository) {

    /** Returns the full detection history ordered by timestamp descending. */
    operator fun invoke(): Flow<List<DetectionEvent>> = detectionRepository.getDetectionEvents()

    /** Returns detection events filtered by type. */
    fun byType(type: DetectionType): Flow<List<DetectionEvent>> =
        detectionRepository.getDetectionEventsByType(type)

    /** Returns detection events for a specific camera. */
    fun byCamera(cameraId: String): Flow<List<DetectionEvent>> =
        detectionRepository.getDetectionEventsByCamera(cameraId)

    /** Returns detection events within a time range. */
    fun inRange(startTime: Long, endTime: Long): Flow<List<DetectionEvent>> =
        detectionRepository.getDetectionEventsInRange(startTime, endTime)

    /** Returns the total detection event count. */
    fun getCount(): Flow<Int> = detectionRepository.getDetectionEventCount()
}
