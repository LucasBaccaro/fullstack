package com.baccaro.lucas.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccaro.lucas.home.remote.TopicsRepository
import com.baccaro.lucas.home.domain.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TopicsState {
    object Idle : TopicsState()
    object Loading : TopicsState()
    data class Success(val topics: List<Topic>) : TopicsState()
    data class Error(val message: String) : TopicsState()
}

class TopicsViewModel(private val topicsRepository: TopicsRepository) : ViewModel() {
    private val _topicsState = MutableStateFlow<TopicsState>(TopicsState.Idle)
    val topicsState = _topicsState.asStateFlow()

    fun getTopics() {
        viewModelScope.launch {
            _topicsState.value = TopicsState.Loading
            val result = topicsRepository.getTopics()
            when {
                result.data != null -> _topicsState.value = TopicsState.Success(result.data)
                result.errorMessage != null -> _topicsState.value =
                    TopicsState.Error(result.errorMessage)
                result.networkException != null -> _topicsState.value =
                    TopicsState.Error("Error de red: ${result.networkException.message}")
                else -> _topicsState.value = TopicsState.Error("Error desconocido")
            }
        }
    }

    fun getCompletedTopics() {
        viewModelScope.launch {
            _topicsState.value = TopicsState.Loading
            val result = topicsRepository.getCompletedTopics()
            when {
                result.data != null -> _topicsState.value = TopicsState.Success(result.data)
                result.errorMessage != null -> _topicsState.value =
                    TopicsState.Error(result.errorMessage)
                result.networkException != null -> _topicsState.value =
                    TopicsState.Error("Error de red: ${result.networkException.message}")
                else -> _topicsState.value = TopicsState.Error("Error desconocido")
            }
        }
    }

    fun reset() {
        _topicsState.value = TopicsState.Idle
    }
} 