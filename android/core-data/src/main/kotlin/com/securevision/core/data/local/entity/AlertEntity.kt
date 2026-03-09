package com.securevision.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.securevision.core.domain.model.Alert
import com.securevision.core.domain.model.AlertSeverity
import com.securevision.core.domain.model.DetectionType

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String,
    val severity: String,
    val timestamp: Long,
    val cameraId: String,
    val thumbnailPath: String?,
    val isRead: Boolean,
    val detectionType: String
) {
    fun toDomain(): Alert = Alert(
        id = id,
        title = title,
        description = description,
        severity = AlertSeverity.valueOf(severity),
        timestamp = timestamp,
        cameraId = cameraId,
        thumbnailPath = thumbnailPath,
        isRead = isRead,
        detectionType = DetectionType.valueOf(detectionType)
    )

    companion object {
        fun fromDomain(alert: Alert): AlertEntity = AlertEntity(
            id = alert.id,
            title = alert.title,
            description = alert.description,
            severity = alert.severity.name,
            timestamp = alert.timestamp,
            cameraId = alert.cameraId,
            thumbnailPath = alert.thumbnailPath,
            isRead = alert.isRead,
            detectionType = alert.detectionType.name
        )
    }
}
