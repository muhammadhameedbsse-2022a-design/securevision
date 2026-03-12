package com.securevision.ml.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundingBoxTest {

    @Test
    fun `width and height computed correctly`() {
        val box = BoundingBox(left = 0.1f, top = 0.2f, right = 0.5f, bottom = 0.8f)
        assertEquals(0.4f, box.width, 0.0001f)
        assertEquals(0.6f, box.height, 0.0001f)
    }

    @Test
    fun `center computed correctly`() {
        val box = BoundingBox(left = 0.0f, top = 0.0f, right = 1.0f, bottom = 1.0f)
        assertEquals(0.5f, box.centerX, 0.0001f)
        assertEquals(0.5f, box.centerY, 0.0001f)
    }

    @Test
    fun `area computed correctly`() {
        val box = BoundingBox(left = 0.1f, top = 0.2f, right = 0.5f, bottom = 0.7f)
        assertEquals(0.4f * 0.5f, box.area, 0.0001f)
    }

    @Test
    fun `valid box returns true`() {
        val box = BoundingBox(left = 0.0f, top = 0.0f, right = 0.5f, bottom = 0.5f)
        assertTrue(box.isValid())
    }

    @Test
    fun `box with negative left is invalid`() {
        val box = BoundingBox(left = -0.1f, top = 0.0f, right = 0.5f, bottom = 0.5f)
        assertFalse(box.isValid())
    }

    @Test
    fun `box exceeding 1 is invalid`() {
        val box = BoundingBox(left = 0.0f, top = 0.0f, right = 1.1f, bottom = 0.5f)
        assertFalse(box.isValid())
    }

    @Test
    fun `zero-width box is invalid`() {
        val box = BoundingBox(left = 0.5f, top = 0.0f, right = 0.5f, bottom = 0.5f)
        assertFalse(box.isValid())
    }

    @Test
    fun `fromAbsolute normalises coordinates`() {
        val box = BoundingBox.fromAbsolute(
            leftPx = 100f, topPx = 200f,
            rightPx = 300f, bottomPx = 400f,
            imageWidth = 1000, imageHeight = 1000
        )
        assertEquals(0.1f, box.left, 0.0001f)
        assertEquals(0.2f, box.top, 0.0001f)
        assertEquals(0.3f, box.right, 0.0001f)
        assertEquals(0.4f, box.bottom, 0.0001f)
    }
}
