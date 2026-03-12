package com.securevision.ml.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectionResultTest {

    @Test
    fun `Success result contains detections`() {
        val detection = Detection(
            label = "Face",
            confidence = 0.95f,
            boundingBox = BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)
        )
        val result = DetectionResult.Success(listOf(detection))
        assertEquals(1, result.detections.size)
        assertEquals("Face", result.detections[0].label)
        assertEquals(0.95f, result.detections[0].confidence, 0.0001f)
    }

    @Test
    fun `Empty result is singleton`() {
        val result = DetectionResult.Empty
        assertTrue(result is DetectionResult.Empty)
    }

    @Test
    fun `Error result contains message and cause`() {
        val cause = RuntimeException("test error")
        val result = DetectionResult.Error("Detection failed", cause)
        assertEquals("Detection failed", result.message)
        assertEquals(cause, result.cause)
    }

    @Test
    fun `Error result cause is optional`() {
        val result = DetectionResult.Error("Detection failed")
        assertEquals("Detection failed", result.message)
        assertNull(result.cause)
    }

    @Test
    fun `NotInitialized result is singleton`() {
        val result = DetectionResult.NotInitialized
        assertTrue(result is DetectionResult.NotInitialized)
    }

    @Test
    fun `Detection metadata defaults to empty map`() {
        val detection = Detection(label = "Face", confidence = 0.9f)
        assertTrue(detection.metadata.isEmpty())
        assertNull(detection.boundingBox)
    }

    @Test
    fun `Detection with metadata stores key-value pairs`() {
        val detection = Detection(
            label = "Face",
            confidence = 0.9f,
            metadata = mapOf("trackingId" to "42", "headPitch" to "5.0")
        )
        assertEquals("42", detection.metadata["trackingId"])
        assertEquals("5.0", detection.metadata["headPitch"])
    }
}
