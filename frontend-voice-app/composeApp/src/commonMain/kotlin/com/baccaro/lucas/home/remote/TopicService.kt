package com.baccaro.lucas.home.remote

import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.core.KtorApi
import com.baccaro.lucas.core.handleApiResponse
import com.baccaro.lucas.home.domain.Topic
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class TopicService(private val api: KtorApi, private val json: Json) {
    suspend fun getTopics(token: String): ApiResult<List<Topic>> {
        val response = api.client.get("${KtorApi.BASE_URL}topics") {
            header("Authorization", "Bearer $token")
        }
        return handleApiResponse(response, ListSerializer(Topic.serializer()), json)
    }

    suspend fun getCompletedTopics(token: String): ApiResult<List<Topic>> {
        val response = api.client.get("${KtorApi.BASE_URL}topics/completed") {
            header("Authorization", "Bearer $token")
        }
        return handleApiResponse(response, ListSerializer(Topic.serializer()), json)
    }
} 