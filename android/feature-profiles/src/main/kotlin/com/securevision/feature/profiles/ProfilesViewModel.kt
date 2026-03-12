package com.securevision.feature.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevision.core.domain.model.AccessLevel
import com.securevision.core.domain.model.Profile
import com.securevision.core.domain.repository.ProfileRepository
import com.securevision.core.domain.usecase.GetProfilesUseCase
import com.securevision.core.domain.usecase.SaveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfilesUiState(
    val isLoading: Boolean = true,
    val profiles: List<Profile> = emptyList(),
    val filteredProfiles: List<Profile> = emptyList(),
    val searchQuery: String = "",
    val showWatchlistOnly: Boolean = false,
    val profileSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilesUiState())
    val uiState: StateFlow<ProfilesUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            getProfilesUseCase().collect { profiles ->
                _uiState.update { state ->
                    val filtered = applyFilters(profiles, state.searchQuery, state.showWatchlistOnly)
                    state.copy(isLoading = false, profiles = profiles, filteredProfiles = filtered)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.profiles, query, state.showWatchlistOnly)
            state.copy(searchQuery = query, filteredProfiles = filtered)
        }
    }

    fun toggleWatchlistFilter() {
        _uiState.update { state ->
            val newFilter = !state.showWatchlistOnly
            val filtered = applyFilters(state.profiles, state.searchQuery, newFilter)
            state.copy(showWatchlistOnly = newFilter, filteredProfiles = filtered)
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
        }
    }

    /**
     * Saves a new profile with an optional face embedding.
     */
    fun saveProfile(
        name: String,
        description: String = "",
        age: Int? = null,
        gender: String? = null,
        embedding: FloatArray? = null,
        isWatchlisted: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val profile = Profile(
                    name = name,
                    description = description,
                    isWatchlisted = isWatchlisted,
                    accessLevel = AccessLevel.STANDARD,
                    createdAt = now,
                    updatedAt = now,
                    age = age,
                    gender = gender,
                    embedding = embedding
                )
                saveProfileUseCase(profile)
                _uiState.update { it.copy(profileSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearProfileSavedFlag() {
        _uiState.update { it.copy(profileSaved = false) }
    }

    private fun applyFilters(
        profiles: List<Profile>,
        query: String,
        watchlistOnly: Boolean
    ): List<Profile> = profiles.filter { profile ->
        val matchesQuery = query.isBlank() ||
                profile.name.contains(query, ignoreCase = true) ||
                profile.description.contains(query, ignoreCase = true)
        val matchesWatchlist = !watchlistOnly || profile.isWatchlisted
        matchesQuery && matchesWatchlist
    }
}
