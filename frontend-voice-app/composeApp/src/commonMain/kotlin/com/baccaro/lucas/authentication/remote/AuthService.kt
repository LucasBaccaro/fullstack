package com.baccaro.lucas.authentication.remote

import com.baccaro.lucas.authentication.domain.AuthRequest
import com.baccaro.lucas.authentication.domain.AuthResponse
import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.core.KtorApi
import com.baccaro.lucas.core.handleApiResponse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class AuthService(private val api: KtorApi, private val json: Json) {

    suspend fun signUp(request: AuthRequest): ApiResult<AuthResponse> {
        val response = api.client.post("${KtorApi.BASE_URL}auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handleApiResponse(response, AuthResponse.serializer(), json)
    }

    suspend fun signIn(request: AuthRequest): ApiResult<AuthResponse> {
        val response = api.client.post("${KtorApi.BASE_URL}auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handleApiResponse(response, AuthResponse.serializer(), json)
    }
}
