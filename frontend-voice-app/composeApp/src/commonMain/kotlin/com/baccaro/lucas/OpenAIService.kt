package com.baccaro.lucas

import com.baccaro.lucas.core.KtorApi
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class OpenAIService(private val api: KtorApi, private val json: Json) {

    suspend fun getEphemeralKey(token: String, instructions: String): String? {
        val response = api.client.post("${KtorApi.BASE_URL}openai/ephemeral-key") {
            contentType(ContentType.Application.Json)
            setBody("""{"instructions": "$instructions"}""")
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.isSuccess()) {
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            return json["client_secret"]?.jsonPrimitive?.contentOrNull
        }
        return null
    }
}