package com.baccaro.lucas.core

import com.baccaro.lucas.authentication.domain.SimpleErrorResponse
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

suspend fun <T> handleApiResponse(
    response: HttpResponse,
    successSerializer: KSerializer<T>,
    json: Json
): ApiResult<T> {
    return try {
        val responseBody = response.bodyAsText()
        if (response.status.isSuccess()) {
            val data = json.decodeFromString(successSerializer, responseBody)
            ApiResult.success(data)
        } else {
            val errorMsg = try {
                json.decodeFromString(SimpleErrorResponse.serializer(), responseBody).detail
            } catch (e: Exception) {
                responseBody
            }
            ApiResult.error(response.status.value, errorMsg)
        }
    } catch (e: Exception) {
        ApiResult.networkError(e)
    }
} 