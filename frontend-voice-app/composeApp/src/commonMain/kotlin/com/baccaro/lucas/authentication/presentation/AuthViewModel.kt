package com.baccaro.lucas.authentication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccaro.lucas.authentication.remote.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUp(email, password)
            when {
                result.data != null -> _authState.value = AuthState.Success
                result.errorMessage != null -> _authState.value =
                    AuthState.Error(result.errorMessage)
                result.networkException != null -> _authState.value =
                    AuthState.Error("Error de red: ${result.networkException.message}")
                else -> _authState.value = AuthState.Error("Error desconocido")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signIn(email, password)
            when {
                result.data != null -> _authState.value = AuthState.Success
                result.errorMessage != null -> _authState.value =
                    AuthState.Error(result.errorMessage)
                result.networkException != null -> _authState.value =
                    AuthState.Error("Error de red: ${result.networkException.message}")
                else -> _authState.value = AuthState.Error("Error desconocido")
            }
        }
    }

    fun reset() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}