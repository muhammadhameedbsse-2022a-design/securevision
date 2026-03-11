package com.securevision.core.domain.model

/**
 * Result of matching a face embedding against known profiles.
 */
data class MatchResult(
    /** The matched profile, or null if no match was found above the threshold. */
    val profile: Profile?,
    /** Cosine similarity score of the best match. */
    val similarity: Float,
    /** Whether this is considered a positive match. */
    val isMatch: Boolean
) {
    companion object {
        /** Default similarity threshold for considering a face as "matched". */
        const val DEFAULT_THRESHOLD = 0.75f

        fun noMatch(): MatchResult = MatchResult(
            profile = null,
            similarity = 0f,
            isMatch = false
        )
    }
}
