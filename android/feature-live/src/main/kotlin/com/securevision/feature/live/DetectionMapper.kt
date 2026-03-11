package com.securevision.feature.live

import com.securevision.ml.common.BoundingBox
import com.securevision.ml.common.Detection

/**
 * Utility object that filters raw [Detection] results by confidence and
 * bounding-box quality, then maps them to [BoundingBox] instances ready
 * for overlay rendering.
 */
object DetectionMapper {

    /**
     * Default minimum normalised area (width × height) for a face bounding box.
     * Coordinates are in the [0, 1] range relative to image dimensions, so an
     * area of 0.005 corresponds to roughly 0.5 % of the image.
     */
    private const val DEFAULT_MIN_FACE_AREA = 0.005f

    /**
     * Filters [detections] by [minConfidence] and a minimum face bounding-box
     * area ([minFaceArea]), returning only those that pass both checks.
     */
    fun filterByQuality(
        detections: List<Detection>,
        minConfidence: Float = 0.7f,
        minFaceArea: Float = DEFAULT_MIN_FACE_AREA
    ): List<Detection> = detections.filter { detection ->
        detection.confidence >= minConfidence &&
            detection.boundingBox?.let { it.area >= minFaceArea } == true
    }

    /**
     * Extracts non-null [BoundingBox] instances from a list of [Detection]s.
     */
    fun toBoundingBoxes(detections: List<Detection>): List<BoundingBox> =
        detections.mapNotNull { it.boundingBox }
}
