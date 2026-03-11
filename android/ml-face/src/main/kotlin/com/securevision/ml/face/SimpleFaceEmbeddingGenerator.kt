package com.securevision.ml.face

import com.securevision.ml.common.Detection
import com.securevision.ml.common.FaceEmbeddingGenerator
import kotlin.math.sqrt

/**
 * Generates a simple face embedding from ML Kit face detection metadata.
 *
 * This implementation builds a normalised feature vector from the available face
 * attributes (head pose, classification probabilities, bounding box proportions).
 * It serves as an architectural foundation — swap in a neural-network-based
 * embedding model (e.g. FaceNet, ArcFace) for production-quality recognition.
 */
class SimpleFaceEmbeddingGenerator : FaceEmbeddingGenerator {

    override val embeddingSize: Int = EMBEDDING_DIM

    override fun generateEmbedding(detection: Detection): FloatArray? {
        val metadata = detection.metadata
        val box = detection.boundingBox ?: return null

        // Extract available face attributes from metadata
        val headPitch = metadata["headPitch"]?.toFloatOrNull() ?: return null
        val headYaw = metadata["headYaw"]?.toFloatOrNull() ?: return null
        val headRoll = metadata["headRoll"]?.toFloatOrNull() ?: return null
        val smileProbability = metadata["smilingProbability"]?.toFloatOrNull() ?: 0f
        val leftEyeOpen = metadata["leftEyeOpenProbability"]?.toFloatOrNull() ?: 0f
        val rightEyeOpen = metadata["rightEyeOpenProbability"]?.toFloatOrNull() ?: 0f

        val raw = floatArrayOf(
            headPitch / MAX_HEAD_ANGLE,
            headYaw / MAX_HEAD_ANGLE,
            headRoll / MAX_HEAD_ANGLE,
            smileProbability,
            leftEyeOpen,
            rightEyeOpen,
            box.width / (box.height + EPSILON),  // aspect ratio
            box.area.coerceIn(0f, 1f)            // already normalised; clamped for safety
        )

        return l2Normalize(raw)
    }

    private fun l2Normalize(vector: FloatArray): FloatArray {
        var sumSq = 0f
        for (v in vector) sumSq += v * v
        val norm = sqrt(sumSq)
        return if (norm > 0f) FloatArray(vector.size) { vector[it] / norm } else vector
    }

    companion object {
        private const val EMBEDDING_DIM = 8
        private const val MAX_HEAD_ANGLE = 45f
        private const val EPSILON = 1e-6f
    }
}
