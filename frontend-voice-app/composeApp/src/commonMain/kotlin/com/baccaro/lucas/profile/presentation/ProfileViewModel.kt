package com.baccaro.lucas.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccaro.lucas.profile.domain.Profile
import com.baccaro.lucas.profile.remote.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val profile: Profile) : ProfileState()
    data class Error(val message: String) : ProfileState()
    object Updated : ProfileState()
}

class ProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState = _profileState.asStateFlow()

    fun getProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val result = profileRepository.getProfile()
            when {
                result.data != null -> _profileState.value = ProfileState.Success(result.data)
                result.errorMessage != null -> _profileState.value =
                    ProfileState.Error(result.errorMessage)
                result.networkException != null -> _profileState.value =
                    ProfileState.Error("Error de red: ${result.networkException.message}")
                else -> _profileState.value = ProfileState.Error("Error desconocido")
            }
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val result = profileRepository.updateProfile(profile)
            when {
                result.data != null -> _profileState.value = ProfileState.Updated
                result.errorMessage != null -> _profileState.value =
                    ProfileState.Error(result.errorMessage)
                result.networkException != null -> _profileState.value =
                    ProfileState.Error("Error de red: ${result.networkException.message}")
                else -> _profileState.value = ProfileState.Error("Error desconocido")
            }
        }
    }

    fun reset() {
        _profileState.value = ProfileState.Idle
    }
} 