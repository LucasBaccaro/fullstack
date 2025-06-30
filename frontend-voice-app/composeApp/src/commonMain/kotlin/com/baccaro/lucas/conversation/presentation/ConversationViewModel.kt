package com.baccaro.lucas.conversation.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccaro.lucas.conversation.remote.ConversationRepository
import com.baccaro.lucas.platform.AudioHelper
import com.baccaro.lucas.platform.BaseInterviewWebRTCClient
import com.baccaro.lucas.platform.createRtcClient
import com.baccaro.lucas.progress.model.PartialProgressReport
import com.baccaro.lucas.progress.model.ProgressReport
import com.baccaro.lucas.progress.remote.ProgressRepository
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// DATA CLASSES PARA EVENTOS DEL SERVIDOR
@Serializable
data class FullServerEvent(
    val type: String,
    val response: ResponseObject? = null,
    val delta: String? = null
)

@Serializable
data class ResponseObject(val output: List<OutputObject>? = null)

@Serializable
data class OutputObject(val type: String, val name: String? = null, val arguments: String? = null)

@Serializable
data class FlashlightArgs(val is_on: Boolean)


// ESTADO Y EVENTOS DE LA UI
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
    val finalReportData: ProgressReport? = null,
    val isAiSpeaking: Boolean = false,
    val aiResponseText: String = "",
    val aiFinalTranscript: String = ""
)

sealed class InterviewEvent {
    data class UpdateUserPrompt(val instructions: String) : InterviewEvent()
    data class StartOrStopInterview(val platformContext: Any, val instructions: String) :
        InterviewEvent()

    data class PermissionResult(val isGranted: Boolean) : InterviewEvent()
    object Reset : InterviewEvent()
}


class ConversationViewModel(
    private val repository: ConversationRepository,
    private val progressRepository: ProgressRepository,
    private val jsonSerializer: Json
) : ViewModel() {

    var uiState by mutableStateOf(InterviewUiState())
        private set

    private var audioHelper: AudioHelper? = null
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
        audioHelper = AudioHelper(platformContext)

        viewModelScope.launch {
            uiState = uiState.copy(
                connectionState = ConnectionState.RequestingToken,
                statusMessage = "Obteniendo token de sesión..."
            )
            try {
                val ephemeralKey = fetchEphemeralKey(instructions)
                uiState = uiState.copy(
                    connectionState = ConnectionState.Connecting,
                    statusMessage = "Conectando a la entrevista..."
                )

                rtcClient = createRtcClient(platformContext, ephemeralKey, ::handleServerEvent)
                rtcClient?.connect()
                audioHelper?.setSpeakerphoneOn(true)

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
        println("DEBUG: Evento recibido del servidor -> tipo: ${event.response}")
        when (event.type) {
            "output_audio_buffer.started" -> {
                uiState =
                    uiState.copy(isAiSpeaking = true, aiResponseText = "", aiFinalTranscript = "")
            }

            "output_audio_buffer.stopped" -> {
                uiState = uiState.copy(isAiSpeaking = false)
            }

            "response.audio_transcript.delta" -> {
                event.delta?.let {
                    uiState = uiState.copy(aiResponseText = uiState.aiResponseText + it)
                }
            }

            "response.done" -> handleFunctionCalls(event.response)
        }
    }

    private fun handleFunctionCalls(response: ResponseObject?) {
        val functionCall = response?.output?.firstOrNull { it.type == "function_call" }
        if (functionCall == null) {
            // Fallback por si la estructura cambia o no es un "function_call" directo
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
                val partial = jsonSerializer.decodeFromString<PartialProgressReport>(argumentsJson)
                // 3. Crea el ProgressReport completo
                val report = ProgressReport(
                    session_date = "2025-06-30T15:34:08.850Z",
                    duration_minutes = 12,
                    topics_discussed = partial.topics_discussed,
                    new_vocabulary = partial.new_vocabulary,
                    grammar_points = partial.grammar_points,
                    ai_summary = partial.ai_summary,
                    suggested_level = partial.suggested_level
                )

                val result = progressRepository.saveProgress(report)
                if (result.data != null) {
                    println("Éxito: El informe de progreso se guardó correctamente en el backend.")
                    uiState = uiState.copy(
                        finalReportData = report,
                        statusMessage = "Informe final generado.",
                        connectionState = ConnectionState.Idle,
                        isAiSpeaking = false
                    )
                } else {
                    uiState = uiState.copy(
                        statusMessage = "No se pudo guardar el informe de progreso. Intenta nuevamente.",
                        connectionState = ConnectionState.Idle,
                        isAiSpeaking = false
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    statusMessage = "Error al procesar el informe: ${e.message}",
                    connectionState = ConnectionState.Idle
                )
                e.printStackTrace()
            } finally {
                cleanupWebRTCResources()
            }
        }
    }

    private fun cleanupWebRTCResources() {
        println("Limpiando recursos de WebRTC...")
        audioHelper?.setSpeakerphoneOn(false)
        rtcClient?.disconnect()
        rtcClient = null
    }

    private suspend fun fetchEphemeralKey(instructions: String): String {
        val result = repository.getEphemeralKey(instructions)
        if (result.data != null) {
            return result.data.client_secret.value
        } else {
            val error =
                result.errorMessage ?: result.networkException?.message ?: "Error desconocido"
            throw IOException("Fallo al obtener token: $error")
        }
    }

    public override fun onCleared() {
        super.onCleared()
        println("ViewModel cleared. Desconectando recursos...")
        cleanupWebRTCResources()
    }
}