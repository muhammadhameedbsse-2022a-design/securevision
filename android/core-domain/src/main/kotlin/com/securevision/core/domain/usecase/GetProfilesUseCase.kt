package com.securevision.core.domain.usecase

import com.securevision.core.domain.model.Profile
import com.securevision.core.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class GetProfilesUseCase(private val profileRepository: ProfileRepository) {

    /** Returns all profiles. */
    operator fun invoke(): Flow<List<Profile>> = profileRepository.getProfiles()

    /** Returns only watchlisted profiles. */
    fun getWatchlisted(): Flow<List<Profile>> = profileRepository.getWatchlistedProfiles()

    /** Searches profiles by name or description. */
    suspend fun search(query: String): List<Profile> = profileRepository.searchProfiles(query)

    /** Returns the total profile count. */
    fun getCount(): Flow<Int> = profileRepository.getProfileCount()
}
