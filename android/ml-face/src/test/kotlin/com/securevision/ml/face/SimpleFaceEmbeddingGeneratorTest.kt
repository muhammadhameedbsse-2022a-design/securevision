package com.securevision.ml.face

import com.securevision.ml.common.BoundingBox
import com.securevision.ml.common.Detection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.sqrt

class SimpleFaceEmbeddingGeneratorTest {

    private val generator = SimpleFaceEmbeddingGenerator()

    @Test
    fun `embedding size is 8`() {
        assertEquals(8, generator.embeddingSize)
    }

    @Test
    fun `generates embedding from valid detection`() {
        val detection = createFaceDetection(
            headPitch = "10.0",
            headYaw = "5.0",
            headRoll = "-3.0",
            smilingProbability = "0.8",
            leftEyeOpenProbability = "0.95",
            rightEyeOpenProbability = "0.9"
        )
        val embedding = generator.generateEmbedding(detection)
        assertNotNull(embedding)
        assertEquals(8, embedding!!.size)
    }

    @Test
    fun `embedding is L2 normalized`() {
        val detection = createFaceDetection(
            headPitch = "10.0",
            headYaw = "5.0",
            headRoll = "-3.0",
            smilingProbability = "0.8",
            leftEyeOpenProbability = "0.95",
            rightEyeOpenProbability = "0.9"
        )
        val embedding = generator.generateEmbedding(detection)!!
        val norm = sqrt(embedding.map { it * it }.sum())
        assertEquals(1.0f, norm, 0.001f)
    }

    @Test
    fun `returns null when headPitch is missing`() {
        val detection = Detection(
            label = "Face",
            confidence = 1.0f,
            boundingBox = BoundingBox(0.2f, 0.3f, 0.6f, 0.8f),
            metadata = mapOf(
                "headYaw" to "5.0",
                "headRoll" to "-3.0"
            )
        )
        assertNull(generator.generateEmbedding(detection))
    }

    @Test
    fun `returns null when bounding box is missing`() {
        val detection = Detection(
            label = "Face",
            confidence = 1.0f,
            boundingBox = null,
            metadata = mapOf(
                "headPitch" to "10.0",
                "headYaw" to "5.0",
                "headRoll" to "-3.0"
            )
        )
        assertNull(generator.generateEmbedding(detection))
    }

    @Test
    fun `different detections produce different embeddings`() {
        val detection1 = createFaceDetection(
            headPitch = "10.0", headYaw = "5.0", headRoll = "0.0",
            smilingProbability = "0.8", leftEyeOpenProbability = "0.95", rightEyeOpenProbability = "0.9"
        )
        val detection2 = createFaceDetection(
            headPitch = "-15.0", headYaw = "20.0", headRoll = "10.0",
            smilingProbability = "0.1", leftEyeOpenProbability = "0.3", rightEyeOpenProbability = "0.4"
        )
        val embedding1 = generator.generateEmbedding(detection1)!!
        val embedding2 = generator.generateEmbedding(detection2)!!
        val areDifferent = embedding1.zip(embedding2).any { (a, b) ->
            kotlin.math.abs(a - b) > 0.01f
        }
        assert(areDifferent) { "Embeddings should differ for different detections" }
    }

    private fun createFaceDetection(
        headPitch: String,
        headYaw: String,
        headRoll: String,
        smilingProbability: String,
        leftEyeOpenProbability: String,
        rightEyeOpenProbability: String
    ): Detection = Detection(
        label = "Face",
        confidence = 1.0f,
        boundingBox = BoundingBox(0.2f, 0.3f, 0.6f, 0.8f),
        metadata = mapOf(
            "headPitch" to headPitch,
            "headYaw" to headYaw,
            "headRoll" to headRoll,
            "smilingProbability" to smilingProbability,
            "leftEyeOpenProbability" to leftEyeOpenProbability,
            "rightEyeOpenProbability" to rightEyeOpenProbability
        )
    )
}
