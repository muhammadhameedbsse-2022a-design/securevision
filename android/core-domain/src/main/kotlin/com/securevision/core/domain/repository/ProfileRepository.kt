package com.securevision.core.domain.repository

import com.securevision.core.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfiles(): Flow<List<Profile>>
    fun getWatchlistedProfiles(): Flow<List<Profile>>
    suspend fun getProfileById(id: Long): Profile?
    suspend fun searchProfiles(query: String): List<Profile>
    suspend fun insertProfile(profile: Profile): Long
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(id: Long)
    fun getProfileCount(): Flow<Int>
}
