package com.securevision.core.data.repository

import com.securevision.core.data.local.dao.ProfileDao
import com.securevision.core.data.local.entity.ProfileEntity
import com.securevision.core.domain.model.Profile
import com.securevision.core.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun getProfiles(): Flow<List<Profile>> =
        profileDao.getProfiles().map { it.map(ProfileEntity::toDomain) }

    override fun getWatchlistedProfiles(): Flow<List<Profile>> =
        profileDao.getWatchlistedProfiles().map { it.map(ProfileEntity::toDomain) }

    override suspend fun getProfileById(id: Long): Profile? =
        profileDao.getProfileById(id)?.toDomain()

    override suspend fun searchProfiles(query: String): List<Profile> =
        profileDao.searchProfiles(query).map { it.toDomain() }

    override suspend fun insertProfile(profile: Profile): Long =
        profileDao.insertProfile(ProfileEntity.fromDomain(profile))

    override suspend fun updateProfile(profile: Profile) =
        profileDao.updateProfile(ProfileEntity.fromDomain(profile))

    override suspend fun deleteProfile(id: Long) =
        profileDao.deleteProfile(id)

    override fun getProfileCount(): Flow<Int> =
        profileDao.getProfileCount()
}
