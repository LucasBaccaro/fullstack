package com.baccaro.lucas.progress.remote

import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.core.KtorApi
import com.baccaro.lucas.core.handleApiResponse
import com.baccaro.lucas.profile.domain.Profile
import com.baccaro.lucas.progress.model.ProgressReport
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class ProgressService(private val api: KtorApi, private val json: Json) {

    suspend fun saveProgress(report: ProgressReport, token: String): ApiResult<ProgressReport> {
        val response = api.client.post("${KtorApi.BASE_URL}progress") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(report)
        }
        return handleApiResponse(response, ProgressReport.serializer(), json)
    }
}

