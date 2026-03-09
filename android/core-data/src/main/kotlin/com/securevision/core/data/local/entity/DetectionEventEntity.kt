package com.securevision.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.securevision.core.domain.model.BoundingBoxDomain
import com.securevision.core.domain.model.DetectionEvent
import com.securevision.core.domain.model.DetectionType

@Entity(tableName = "detection_events")
@TypeConverters(MapConverter::class)
data class DetectionEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long,
    val cameraId: String,
    val detectionType: String,
    val confidence: Float,
    val boundingBoxLeft: Float?,
    val boundingBoxTop: Float?,
    val boundingBoxRight: Float?,
    val boundingBoxBottom: Float?,
    val label: String,
    val thumbnailPath: String?,
    val processingTimeMs: Long,
    val metadataJson: String
) {
    fun toDomain(): DetectionEvent {
        val bbox = if (boundingBoxLeft != null && boundingBoxTop != null &&
            boundingBoxRight != null && boundingBoxBottom != null
        ) {
            BoundingBoxDomain(boundingBoxLeft, boundingBoxTop, boundingBoxRight, boundingBoxBottom)
        } else null

        val metadata: Map<String, String> = try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(metadataJson, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }

        return DetectionEvent(
            id = id,
            timestamp = timestamp,
            cameraId = cameraId,
            detectionType = DetectionType.valueOf(detectionType),
            confidence = confidence,
            boundingBox = bbox,
            label = label,
            thumbnailPath = thumbnailPath,
            processingTimeMs = processingTimeMs,
            metadata = metadata
        )
    }

    companion object {
        fun fromDomain(event: DetectionEvent): DetectionEventEntity = DetectionEventEntity(
            id = event.id,
            timestamp = event.timestamp,
            cameraId = event.cameraId,
            detectionType = event.detectionType.name,
            confidence = event.confidence,
            boundingBoxLeft = event.boundingBox?.left,
            boundingBoxTop = event.boundingBox?.top,
            boundingBoxRight = event.boundingBox?.right,
            boundingBoxBottom = event.boundingBox?.bottom,
            label = event.label,
            thumbnailPath = event.thumbnailPath,
            processingTimeMs = event.processingTimeMs,
            metadataJson = Gson().toJson(event.metadata)
        )
    }
}

class MapConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(map: Map<String, String>): String = gson.toJson(map)

    @TypeConverter
    fun toMap(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}
