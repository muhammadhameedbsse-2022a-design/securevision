package com.securevision.core.domain.model

import kotlin.math.sqrt

/**
 * Wraps a face embedding vector and provides similarity comparison.
 * The embedding is a fixed-size float array produced by a face embedding generator.
 */
data class FaceEmbedding(
    val data: FloatArray
) {
    /** Computes cosine similarity between this embedding and [other]. Returns value in [-1, 1]. */
    fun cosineSimilarity(other: FaceEmbedding): Float {
        require(data.size == other.data.size) {
            "Embedding dimensions must match: ${data.size} vs ${other.data.size}"
        }
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in data.indices) {
            dot += data[i] * other.data[i]
            normA += data[i] * data[i]
            normB += other.data[i] * other.data[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom > 0f) dot / denom else 0f
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FaceEmbedding) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int = data.contentHashCode()
}
