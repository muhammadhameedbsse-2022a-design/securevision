package com.securevision.core.domain.model

/**
 * Represents a security alert raised by the detection system.
 */
data class Alert(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val timestamp: Long,
    val cameraId: String,
    val thumbnailPath: String? = null,
    val isRead: Boolean = false,
    val detectionType: DetectionType
)

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class DetectionType {
    FACE_RECOGNIZED,
    FACE_UNKNOWN,
    WEAPON_DETECTED,
    SUSPICIOUS_ATTRIBUTE,
    MOTION_DETECTED,
    PERIMETER_BREACH
}
