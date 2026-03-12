package com.securevision.feature.live

import com.securevision.ml.common.BoundingBox
import com.securevision.ml.common.Detection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectionMapperTest {

    @Test
    fun `filterByQuality removes low confidence detections`() {
        val detections = listOf(
            createDetection("Face", 0.9f, 0.1f, 0.1f, 0.5f, 0.5f),
            createDetection("Face", 0.3f, 0.1f, 0.1f, 0.5f, 0.5f),
            createDetection("Face", 0.8f, 0.1f, 0.1f, 0.5f, 0.5f)
        )
        val filtered = DetectionMapper.filterByQuality(detections, minConfidence = 0.7f)
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.confidence >= 0.7f })
    }

    @Test
    fun `filterByQuality removes small bounding boxes`() {
        val detections = listOf(
            createDetection("Face", 0.9f, 0.49f, 0.49f, 0.5f, 0.5f), // tiny box
            createDetection("Face", 0.9f, 0.1f, 0.1f, 0.5f, 0.5f)   // normal box
        )
        val filtered = DetectionMapper.filterByQuality(detections, minConfidence = 0.5f)
        assertEquals(1, filtered.size)
    }

    @Test
    fun `filterByQuality returns empty list when no detections pass`() {
        val detections = listOf(
            createDetection("Face", 0.3f, 0.1f, 0.1f, 0.5f, 0.5f)
        )
        val filtered = DetectionMapper.filterByQuality(detections, minConfidence = 0.7f)
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `toBoundingBoxes extracts non-null boxes`() {
        val detections = listOf(
            Detection(label = "Face", confidence = 0.9f, boundingBox = BoundingBox(0.1f, 0.2f, 0.3f, 0.4f)),
            Detection(label = "Face", confidence = 0.8f, boundingBox = null),
            Detection(label = "Face", confidence = 0.7f, boundingBox = BoundingBox(0.5f, 0.6f, 0.7f, 0.8f))
        )
        val boxes = DetectionMapper.toBoundingBoxes(detections)
        assertEquals(2, boxes.size)
    }

    @Test
    fun `toBoundingBoxes returns empty for no valid boxes`() {
        val detections = listOf(
            Detection(label = "Face", confidence = 0.9f, boundingBox = null)
        )
        val boxes = DetectionMapper.toBoundingBoxes(detections)
        assertTrue(boxes.isEmpty())
    }

    private fun createDetection(
        label: String,
        confidence: Float,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): Detection = Detection(
        label = label,
        confidence = confidence,
        boundingBox = BoundingBox(left, top, right, bottom)
    )
}
