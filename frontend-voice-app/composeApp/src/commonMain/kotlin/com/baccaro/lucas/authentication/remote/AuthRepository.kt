package com.baccaro.lucas.authentication.remote

import com.baccaro.lucas.authentication.domain.AuthRequest
import com.baccaro.lucas.authentication.domain.AuthResponse
import com.baccaro.lucas.core.ApiResult
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class AuthRepository(
    private val authService: AuthService,
    private val settings: Settings,
) {
    suspend fun signUp(email: String, password: String): ApiResult<AuthResponse> {
        val result = authService.signUp(AuthRequest(email, password))
        if (result.data != null) {
            settings["token"] = result.data.session.access_token
        }
        return result
    }

    suspend fun signIn(email: String, password: String): ApiResult<AuthResponse> {
        val result = authService.signIn(AuthRequest(email, password))
        if (result.data != null) {
            settings["token"] = result.data.session.access_token
        }
        return result
    }

    fun getToken(): String? {
        return settings.getStringOrNull("token")
    }

    fun clearToken() {
        settings.remove("token")
    }
}
