package com.securevision.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.securevision.core.data.local.entity.DetectionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionEventDao {

    @Query("SELECT * FROM detection_events ORDER BY timestamp DESC")
    fun getDetectionEvents(): Flow<List<DetectionEventEntity>>

    @Query("SELECT * FROM detection_events WHERE detectionType = :type ORDER BY timestamp DESC")
    fun getDetectionEventsByType(type: String): Flow<List<DetectionEventEntity>>

    @Query("SELECT * FROM detection_events WHERE cameraId = :cameraId ORDER BY timestamp DESC")
    fun getDetectionEventsByCamera(cameraId: String): Flow<List<DetectionEventEntity>>

    @Query(
        "SELECT * FROM detection_events WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC"
    )
    fun getDetectionEventsInRange(startTime: Long, endTime: Long): Flow<List<DetectionEventEntity>>

    @Query("SELECT * FROM detection_events WHERE id = :id LIMIT 1")
    suspend fun getDetectionEventById(id: Long): DetectionEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectionEvent(event: DetectionEventEntity): Long

    @Query("DELETE FROM detection_events WHERE id = :id")
    suspend fun deleteDetectionEvent(id: Long)

    @Query("DELETE FROM detection_events WHERE timestamp < :timestamp")
    suspend fun deleteDetectionEventsOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM detection_events")
    fun getDetectionEventCount(): Flow<Int>
}
