package com.baccaro.lucas.platform

import com.baccaro.lucas.conversation.presentation.FullServerEvent

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

// La clase base que contiene toda la lógica compartida.
abstract class BaseInterviewWebRTCClient(
    protected val ephemeralKey: String,
    protected val onServerEvent: (FullServerEvent) -> Unit
) {
    protected val jsonSerializer = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val httpClient = HttpClient() // Ktor client es común

    // --- MÉTODOS PÚBLICOS Y COMPARTIDOS ---

    /**
     * Orquestador principal de la conexión. Este flujo es idéntico en ambas plataformas.
     */
    suspend fun connect() {
        try {
            // 1. Configuración de la conexión (específica de la plataforma)
            setupPeerConnection()

            // 2. Crear oferta y establecerla localmente (específico de la plataforma)
            val offerSdp = createOfferAndSetLocalDescription()

            // 3. Intercambiar SDP con el servidor (¡LÓGICA 100% COMPARTIDA!)
            val answerSdp = exchangeSdpWithServer(offerSdp)

            // 4. Establecer la descripción remota (específica de la plataforma)
            setRemoteDescription(answerSdp)

            println("KMP-WebRTC-Base: Handshake completado.")
        } catch (e: Exception) {
            println("KMP-WebRTC-Base: Fallo en conexión: $e")
            disconnect()
        }
    }

    /**
     * Esta lógica es 100% compartida.
     */
    fun requestFinalReport() {
        val triggerMessage = """{"type": "conversation.item.create", "item": { "type": "message", "role": "user", "content": [{ "type": "input_text", "text": "Muchas gracias por tu tiempo. Por favor, genera el informe final ahora de lo que fue la charla." }] }}"""
        println("KMP-WebRTC-Base: --> SEND (Trigger Message)")
        // Delega el envío real a la implementación de la plataforma
        sendDataOnChannel(triggerMessage)

        val requestJson = """{"type":"response.create"}"""
        println("KMP-WebRTC-Base: --> SEND (Request Response)")
        sendDataOnChannel(requestJson)
    }

    /**
     * Esta lógica es 100% compartida.
     */
    fun disconnect() {
        println("KMP-WebRTC-Base: Desconectando...")
        performCleanup() // Limpieza específica de la plataforma
        httpClient.close() // Limpieza común
    }


    // --- LÓGICA INTERNA COMPARTIDA ---

    private suspend fun exchangeSdpWithServer(offerSdp: String): String {
        val response = httpClient.post("https://api.openai.com/v1/realtime") {
            parameter("model", "gpt-4o-mini-realtime-preview-2024-12-17")
            header("Authorization", "Bearer $ephemeralKey")
            contentType(ContentType("application", "sdp"))
            setBody(offerSdp)
        }
        if (response.status.isSuccess()) return response.bodyAsText()
        else throw IOException("Server error: ${response.status} - ${response.bodyAsText()}")
    }


    // --- MÉTODOS ABSTRACTOS (EL CONTRATO QUE LAS PLATAFORMAS DEBEN CUMPLIR) ---

    /** Prepara PeerConnection, DataChannel, AudioTrack, etc. */
    protected abstract fun setupPeerConnection()

    /** Crea la oferta SDP y la establece como descripción local. Devuelve el SDP como String. */
    protected abstract suspend fun createOfferAndSetLocalDescription(): String

    /** Establece la respuesta SDP del servidor como descripción remota. */
    protected abstract suspend fun setRemoteDescription(sdp: String)

    /** Envía un mensaje de texto a través del DataChannel. */
    protected abstract fun sendDataOnChannel(data: String)

    /** Cierra todos los objetos de WebRTC específicos de la plataforma. */
    protected abstract fun performCleanup()
}