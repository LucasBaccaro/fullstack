package com.baccaro.lucas.conversation.remote

import com.baccaro.lucas.conversation.domain.EphemeralKeyRequest
import com.baccaro.lucas.conversation.domain.EphemeralKeyResponse
import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.core.KtorApi
import com.baccaro.lucas.core.handleApiResponse
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class ConversationService(private val api: KtorApi, private val json: Json) {

    suspend fun getEphemeralKey(token: String, instructions: String): ApiResult<EphemeralKeyResponse> {
        val response = api.client.post("${KtorApi.BASE_URL}openai/ephemeral-key") {
            contentType(ContentType.Application.Json)
            setBody(EphemeralKeyRequest(instructions))
            header("Authorization", "Bearer $token")
        }
        return handleApiResponse(response, EphemeralKeyResponse.serializer(), json)
    }
}
