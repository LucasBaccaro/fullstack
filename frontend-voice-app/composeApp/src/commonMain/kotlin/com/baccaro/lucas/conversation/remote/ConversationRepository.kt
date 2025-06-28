package com.baccaro.lucas.conversation.remote

import com.baccaro.lucas.core.ApiResult
import com.baccaro.lucas.conversation.domain.EphemeralKeyResponse
import com.russhwolf.settings.Settings

class ConversationRepository(
    private val conversationService: ConversationService,
    private val settings: Settings
) {
    fun getToken(): String? = settings.getStringOrNull("token")

    suspend fun getEphemeralKey(instructions: String): ApiResult<EphemeralKeyResponse> {
        val token = getToken() ?: return ApiResult.error(401, "No autenticado")
        return conversationService.getEphemeralKey(token, instructions)
    }
}
