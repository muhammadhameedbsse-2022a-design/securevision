package com.securevision.ml.common

import android.graphics.Bitmap

/**
 * Contract for all ML classifiers / detectors in the SecureVision pipeline.
 */
interface Classifier {

    /**
     * Initialise the classifier (load models, configure client, etc.).
     * Must be called before [classify]. May be called multiple times safely.
     */
    suspend fun initialize()

    /**
     * Run inference on [bitmap].
     *
     * @param bitmap   Input image. Must not be recycled.
     * @param rotation Clockwise rotation degrees applied to the bitmap before inference (0, 90, 180, 270).
     * @return [DetectionResult] wrapping all detected objects or an error state.
     */
    suspend fun classify(bitmap: Bitmap, rotation: Int = 0): DetectionResult

    /**
     * Release any native/ML resources held by this classifier.
     * The classifier must not be used after this call without re-initialising.
     */
    fun close()

    /** Whether the classifier has been successfully initialised and is ready to process frames. */
    val isInitialized: Boolean

    /** Minimum confidence threshold below which detections are discarded. */
    val confidenceThreshold: Float
}
