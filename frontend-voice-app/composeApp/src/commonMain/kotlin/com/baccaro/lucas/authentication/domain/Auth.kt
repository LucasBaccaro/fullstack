package com.baccaro.lucas.authentication.domain

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(
    val user: User,
    val session: Session
)

@Serializable
data class User(
    val id: String,
    val email: String
)

@Serializable
data class Session(
    val access_token: String,
    val token_type: String
)

// Para errores como el 400, donde 'detail' es un String
@Serializable
data class SimpleErrorResponse(
    val detail: String
)

// Para errores como el 422, donde 'detail' es una lista de objetos
@Serializable
data class ValidationErrorResponse(
    val detail: List<ValidationErrorDetail>
)

@Serializable
data class ValidationErrorDetail(
    val msg: String
)