package com.securevision.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetectionEventTest {

    @Test
    fun `detection event with all fields`() {
        val event = DetectionEvent(
            id = 1L,
            timestamp = 1000L,
            cameraId = "back",
            detectionType = DetectionType.FACE_RECOGNIZED,
            confidence = 0.95f,
            boundingBox = BoundingBoxDomain(0.1f, 0.2f, 0.5f, 0.8f),
            label = "John Doe",
            thumbnailPath = "/path/to/thumb",
            processingTimeMs = 50L,
            age = 30,
            gender = "Male",
            metadata = mapOf("trackingId" to "42")
        )

        assertEquals(1L, event.id)
        assertEquals("back", event.cameraId)
        assertEquals(DetectionType.FACE_RECOGNIZED, event.detectionType)
        assertEquals(0.95f, event.confidence, 0.0001f)
        assertEquals(30, event.age)
        assertEquals("Male", event.gender)
    }

    @Test
    fun `detection event defaults for optional fields`() {
        val event = DetectionEvent(
            timestamp = 1000L,
            cameraId = "front",
            detectionType = DetectionType.FACE_UNKNOWN,
            confidence = 1.0f,
            label = "Face"
        )

        assertEquals(0L, event.id)
        assertNull(event.boundingBox)
        assertNull(event.thumbnailPath)
        assertEquals(0L, event.processingTimeMs)
        assertNull(event.age)
        assertNull(event.gender)
        assertEquals(emptyMap<String, String>(), event.metadata)
    }

    @Test
    fun `bounding box domain properties computed correctly`() {
        val bbox = BoundingBoxDomain(0.1f, 0.2f, 0.5f, 0.8f)
        assertEquals(0.4f, bbox.width, 0.0001f)
        assertEquals(0.6f, bbox.height, 0.0001f)
        assertEquals(0.3f, bbox.centerX, 0.0001f)
        assertEquals(0.5f, bbox.centerY, 0.0001f)
    }

    @Test
    fun `all detection types are valid`() {
        val types = DetectionType.values()
        assertEquals(6, types.size)
        assert(types.contains(DetectionType.FACE_RECOGNIZED))
        assert(types.contains(DetectionType.FACE_UNKNOWN))
        assert(types.contains(DetectionType.WEAPON_DETECTED))
        assert(types.contains(DetectionType.SUSPICIOUS_ATTRIBUTE))
        assert(types.contains(DetectionType.MOTION_DETECTED))
        assert(types.contains(DetectionType.PERIMETER_BREACH))
    }
}
