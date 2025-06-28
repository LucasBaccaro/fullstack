package com.baccaro.lucas.home.remote

import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.home.domain.Topic
import com.russhwolf.settings.Settings

class TopicsRepository(
    private val topicService: TopicService,
    private val settings: Settings
) {
    fun getToken(): String? = settings.getStringOrNull("token")

    suspend fun getTopics(): ApiResult<List<Topic>> {
        val token = getToken() ?: return ApiResult.error(401, "No autenticado")
        return topicService.getTopics(token)
    }

    suspend fun getCompletedTopics(): ApiResult<List<Topic>> {
        val token = getToken() ?: return ApiResult.error(401, "No autenticado")
        return topicService.getCompletedTopics(token)
    }
} 