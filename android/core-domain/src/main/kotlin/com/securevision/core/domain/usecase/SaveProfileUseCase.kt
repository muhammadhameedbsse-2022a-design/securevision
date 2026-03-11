package com.securevision.core.domain.usecase

import com.securevision.core.domain.model.Profile
import com.securevision.core.domain.repository.ProfileRepository

/**
 * Saves a profile (with optional face embedding) to the local store.
 */
class SaveProfileUseCase(private val profileRepository: ProfileRepository) {

    suspend operator fun invoke(profile: Profile): Long =
        profileRepository.insertProfile(profile)
}
