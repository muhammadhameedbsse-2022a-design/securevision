package com.securevision.ml.common

/**
 * Generates a face embedding vector from detection metadata.
 * Implementations may use ML-model-based approaches (e.g. FaceNet, ArcFace)
 * or simpler attribute-based approaches for development/testing.
 */
interface FaceEmbeddingGenerator {

    /** The dimensionality of the embeddings produced by this generator. */
    val embeddingSize: Int

    /**
     * Generates a face embedding from the given [detection] metadata.
     * Returns a float array of size [embeddingSize], or null if the detection
     * does not contain enough information to produce an embedding.
     */
    fun generateEmbedding(detection: Detection): FloatArray?
}
