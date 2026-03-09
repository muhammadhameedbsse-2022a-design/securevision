package com.securevision.core.domain.model

/**
 * Represents a single detection event captured from a camera feed.
 */
data class DetectionEvent(
    val id: Long = 0L,
    val timestamp: Long,
    val cameraId: String,
    val detectionType: DetectionType,
    val confidence: Float,
    val boundingBox: BoundingBoxDomain? = null,
    val label: String,
    val thumbnailPath: String? = null,
    val processingTimeMs: Long = 0L,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Lightweight bounding-box representation for the domain layer (no Android dependencies).
 */
data class BoundingBoxDomain(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = left + width / 2f
    val centerY: Float get() = top + height / 2f
}
