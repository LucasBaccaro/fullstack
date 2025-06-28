package com.baccaro.lucas.conversation.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccaro.lucas.conversation.remote.ConversationRepository
import com.baccaro.lucas.platform.BaseInterviewWebRTCClient
import com.baccaro.lucas.platform.createRtcClient
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// DATA CLASSES (deberían estar en su propio archivo de dominio)
@Serializable
data class FullServerEvent(val type: String, val response: ResponseObject? = null, val delta: String? = null)
@Serializable
data class ResponseObject(val output: List<OutputObject>? = null)
@Serializable
data class OutputObject(val type: String, val name: String? = null, val arguments: String? = null)
@Serializable
data class FinalReport(
    val overall_score: Int,
    val summary: String,
    val strengths: List<String>,
    val areas_for_improvement: List<String>,
    val english_level: String
)
@Serializable
data class FlashlightArgs(val is_on: Boolean)


// VIEWMODEL STATE & EVENTS
sealed class ConnectionState {
    object Idle : ConnectionState()
    object RequestingToken : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class InterviewUiState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val statusMessage: String = "Listo para la entrevista.",
    val userPrompt: String = "",
    val hasPermission: Boolean = false,
    val finalReportData: FinalReport? = null,
    val isAiSpeaking: Boolean = false,
    val aiResponseText: String = "",
    val aiFinalTranscript: String = ""
)

sealed class InterviewEvent {
    data class UpdateUserPrompt(val instructions: String) : InterviewEvent()
    data class StartOrStopInterview(val platformContext: Any, val instructions: String) : InterviewEvent()
    data class PermissionResult(val isGranted: Boolean) : InterviewEvent()
    object Reset : InterviewEvent()
}


class ConversationViewModel(
    private val repository: ConversationRepository,
    private val jsonSerializer: Json
) : ViewModel() {

    var uiState by mutableStateOf(InterviewUiState())
        private set

    private var rtcClient: BaseInterviewWebRTCClient? = null

    fun onEvent(event: InterviewEvent) {
        when (event) {
            is InterviewEvent.UpdateUserPrompt -> {
                uiState = uiState.copy(userPrompt = event.instructions)
            }
            is InterviewEvent.StartOrStopInterview -> {
                if (uiState.connectionState == ConnectionState.Connected) {
                    stopAndGenerateReport()
                } else {
                    startInterview(event.platformContext, event.instructions)
                }
            }
            is InterviewEvent.PermissionResult -> {
                uiState = uiState.copy(hasPermission = event.isGranted)
                if (!event.isGranted) {
                    uiState = uiState.copy(
                        connectionState = ConnectionState.Error("Permiso denegado."),
                        statusMessage = "Se requieren permisos de cámara y micrófono."
                    )
                }
            }
            is InterviewEvent.Reset -> {
                cleanupWebRTCResources()
                uiState = InterviewUiState(hasPermission = uiState.hasPermission)
            }
        }
    }

    private fun startInterview(platformContext: Any, instructions: String) {
        if (!uiState.hasPermission) {
            uiState = uiState.copy(statusMessage = "Por favor, concede los permisos necesarios.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                connectionState = ConnectionState.RequestingToken,
                statusMessage = "Obteniendo token de sesión..."
            )
            try {
                val ephemeralKey = fetchEphemeralKey(instructions) // <-- USA EL PROMPT DEL PARÁMETRO
                uiState = uiState.copy(
                    connectionState = ConnectionState.Connecting,
                    statusMessage = "Conectando a la entrevista..."
                )

                rtcClient = createRtcClient(platformContext, ephemeralKey, ::handleServerEvent)
                rtcClient?.connect() // Es suspend

                uiState = uiState.copy(
                    connectionState = ConnectionState.Connected,
                    statusMessage = "Conectado. La entrevista ha comenzado."
                )

            } catch (e: Exception) {
                val errorMessage = "Fallo al iniciar: ${e.message}"
                uiState = uiState.copy(
                    connectionState = ConnectionState.Error(errorMessage),
                    statusMessage = errorMessage
                )
                e.printStackTrace()
            }
        }
    }

    private fun stopAndGenerateReport() {
        uiState = uiState.copy(statusMessage = "Finalizando y generando informe...")
        rtcClient?.requestFinalReport()
    }

    private fun handleServerEvent(event: FullServerEvent) {
        println("DEBUG: Evento recibido del servidor -> tipo: ${event.type}")
        when (event.type) {
            "output_audio_buffer.started" -> {
                uiState = uiState.copy(isAiSpeaking = true, aiResponseText = "", aiFinalTranscript = "")
            }
            "output_audio_buffer.stopped" -> {
                uiState = uiState.copy(isAiSpeaking = false)
            }
            "response.audio_transcript.delta" -> {
                event.delta?.let {
                    uiState = uiState.copy(aiResponseText = uiState.aiResponseText + it)
                }
            }
            "response.audio_transcript.done" -> {
                /*event.delta?.let {
                    uiState = uiState.copy(aiFinalTranscript = it, aiResponseText = "")
                }*/
            }
            "response.done" -> handleFunctionCalls(event.response)
        }
    }

    private fun handleFunctionCalls(response: ResponseObject?) {
        val functionCall = response?.output?.firstOrNull { it.type == "function_call" }
        if (functionCall == null) {
            response?.output?.firstOrNull { it.name == "generate_final_report" }?.let {
                handleFinalReportCall(it.arguments)
            }
            return
        }

        when (functionCall.name) {
            "generate_final_report" -> handleFinalReportCall(functionCall.arguments)
            else -> println("Llamada a función desconocida recibida: ${functionCall.name}")
        }
    }

    private fun handleFinalReportCall(argumentsJson: String?) {
        if (argumentsJson == null) {
            uiState = uiState.copy(statusMessage = "Error: Informe final sin argumentos.")
            return
        }
        println("Informe final detectado. Argumentos: $argumentsJson")
        viewModelScope.launch {
            try {
                val report = jsonSerializer.decodeFromString<FinalReport>(argumentsJson)
                uiState = uiState.copy(
                    finalReportData = report,
                    statusMessage = "Informe final generado.",
                    connectionState = ConnectionState.Idle,
                    isAiSpeaking = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    statusMessage = "Error al procesar el informe.",
                    connectionState = ConnectionState.Idle
                )
            } finally {
                cleanupWebRTCResources()
            }
        }
    }

    private fun cleanupWebRTCResources() {
        println("Limpiando recursos de WebRTC...")
        rtcClient?.disconnect()
        rtcClient = null
    }

    private suspend fun fetchEphemeralKey(instructions: String): String {
        val result = repository.getEphemeralKey(instructions)
        if (result.data != null) {
            return result.data.client_secret.value
        } else {
            val error = result.errorMessage ?: result.networkException?.message ?: "Error desconocido"
            throw IOException("Fallo al obtener token: $error")
        }
    }

    public override fun onCleared() {
        super.onCleared()
        println("ViewModel cleared. Desconectando recursos...")
        cleanupWebRTCResources()
    }
}
