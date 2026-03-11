package com.securevision.core.domain.usecase

import com.securevision.core.domain.model.FaceEmbedding
import com.securevision.core.domain.model.MatchResult
import com.securevision.core.domain.model.Profile
import com.securevision.core.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first

/**
 * Matches a [FaceEmbedding] against all stored profiles that have embeddings.
 * Returns a [MatchResult] indicating the best match (or no match).
 */
class MatchFaceUseCase(private val profileRepository: ProfileRepository) {

    /**
     * Finds the best-matching profile for the given [embedding].
     * @param embedding the face embedding to match against stored profiles.
     * @param threshold minimum cosine similarity to consider a positive match.
     */
    suspend operator fun invoke(
        embedding: FaceEmbedding,
        threshold: Float = MatchResult.DEFAULT_THRESHOLD
    ): MatchResult {
        val profiles = profileRepository.getProfilesWithEmbeddings().first()
        return findBestMatch(embedding, profiles, threshold)
    }

    private fun findBestMatch(
        embedding: FaceEmbedding,
        profiles: List<Profile>,
        threshold: Float
    ): MatchResult {
        var bestProfile: Profile? = null
        var bestSimilarity = -1f

        for (profile in profiles) {
            val profileEmbedding = profile.embedding ?: continue
            val similarity = embedding.cosineSimilarity(FaceEmbedding(profileEmbedding))
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity
                bestProfile = profile
            }
        }

        return if (bestProfile != null && bestSimilarity >= threshold) {
            MatchResult(
                profile = bestProfile,
                similarity = bestSimilarity,
                isMatch = true
            )
        } else {
            MatchResult(
                profile = bestProfile,
                similarity = if (bestSimilarity >= 0f) bestSimilarity else 0f,
                isMatch = false
            )
        }
    }
}
