package com.baccaro.lucas.progress.remote

import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.profile.domain.Profile
import com.baccaro.lucas.profile.remote.ProfileService
import com.baccaro.lucas.progress.model.ProgressReport
import com.russhwolf.settings.Settings

class ProgressRepository(
    private val progressService: ProgressService,
    private val settings: Settings
) {
    fun getToken(): String? = settings.getStringOrNull("token")

    suspend fun saveProgress(report: ProgressReport): ApiResult<ProgressReport> {
        val token = getToken() ?: return ApiResult.error(401, "No autenticado")
        return progressService.saveProgress(report, token)
    }
}
