package com.baccaro.lucas.profile.remote

import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.profile.domain.Profile
import com.russhwolf.settings.Settings

class ProfileRepository(
    private val profileService: ProfileService,
    private val settings: Settings
) {
    fun getToken(): String? = settings.getStringOrNull("token")

    suspend fun getProfile(): ApiResult<Profile> {
        val token = getToken() ?: return ApiResult.error(401, "No autenticado")
        return profileService.getProfile(token)
    }

    suspend fun updateProfile(profile: Profile): ApiResult<Profile> {
        val token = getToken() ?: return ApiResult.error(401, "No autenticado")
        return profileService.updateProfile(token, profile)
    }
}
