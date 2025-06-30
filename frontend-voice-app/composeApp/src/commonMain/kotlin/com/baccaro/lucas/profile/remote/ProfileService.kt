package com.baccaro.lucas.profile.remote

import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.core.KtorApi
import com.baccaro.lucas.core.handleApiResponse
import com.baccaro.lucas.profile.domain.Profile
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

import com.baccaro.lucas.progress.model.ProgressReport
import kotlinx.serialization.builtins.ListSerializer

class ProfileService(private val api: KtorApi, private val json: Json) {
    suspend fun getProfile(token: String): ApiResult<Profile> {
        val response = api.client.get("${KtorApi.BASE_URL}profile/me") {
            header("Authorization", "Bearer $token")
        }
        return handleApiResponse(response, Profile.serializer(), json)
    }

    suspend fun updateProfile(token: String, profile: Profile): ApiResult<Profile> {
        val response = api.client.patch("${KtorApi.BASE_URL}profile/me") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(profile)
        }
        return handleApiResponse(response, Profile.serializer(), json)
    }

    suspend fun getProgressHistory(token: String): ApiResult<List<ProgressReport>> {
        val response = api.client.get("${KtorApi.BASE_URL}progress") {
            header("Authorization", "Bearer $token")
        }
        return handleApiResponse(response, ListSerializer(ProgressReport.serializer()), json)
    }
}
