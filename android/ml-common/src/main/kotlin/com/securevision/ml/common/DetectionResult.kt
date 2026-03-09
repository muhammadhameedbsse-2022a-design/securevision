package com.securevision.ml.common

/**
 * Sealed hierarchy representing every possible outcome from an ML detection pass.
 */
sealed class DetectionResult {

    /** One or more objects/faces were successfully detected. */
    data class Success(
        val detections: List<Detection>
    ) : DetectionResult()

    /** The frame was processed but nothing was detected above the threshold. */
    object Empty : DetectionResult()

    /** A recoverable error occurred during inference. */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : DetectionResult()

    /** The classifier is not yet initialised. */
    object NotInitialized : DetectionResult()
}

/**
 * A single detection with its label, confidence score, and optional bounding box.
 */
data class Detection(
    /** Human-readable category label. */
    val label: String,
    /** Confidence in [0, 1]. */
    val confidence: Float,
    /** Normalised bounding box, null if the model is classification-only. */
    val boundingBox: BoundingBox? = null,
    /** Arbitrary key-value metadata (e.g. landmarks, attributes). */
    val metadata: Map<String, String> = emptyMap()
)
