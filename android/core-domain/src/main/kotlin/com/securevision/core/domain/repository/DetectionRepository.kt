package com.securevision.core.domain.repository

import com.securevision.core.domain.model.DetectionEvent
import com.securevision.core.domain.model.DetectionType
import kotlinx.coroutines.flow.Flow

interface DetectionRepository {
    fun getDetectionEvents(): Flow<List<DetectionEvent>>
    fun getDetectionEventsByType(type: DetectionType): Flow<List<DetectionEvent>>
    fun getDetectionEventsByCamera(cameraId: String): Flow<List<DetectionEvent>>
    fun getDetectionEventsInRange(startTime: Long, endTime: Long): Flow<List<DetectionEvent>>
    suspend fun getDetectionEventById(id: Long): DetectionEvent?
    suspend fun insertDetectionEvent(event: DetectionEvent): Long
    suspend fun deleteDetectionEvent(id: Long)
    suspend fun deleteDetectionEventsOlderThan(timestamp: Long)
    fun getDetectionEventCount(): Flow<Int>
}
