package com.baccaro.lucas.conversation.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EphemeralKeyRequest(@SerialName("instructions") val instructions: String)

@Serializable
data class EphemeralKeyResponse(
    val success: Boolean,
    val client_secret: ClientSecret
)

@Serializable
data class ClientSecret(
    val value: String,
    val expires_at: Long
)

@Serializable
data class ConversationErrorResponse(
    val success: Boolean,
    val error: String
)