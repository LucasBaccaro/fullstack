@file:OptIn(ExperimentalForeignApi::class)
package com.baccaro.lucas
import cocoapods.WebRTC.RTCAudioTrack
import cocoapods.WebRTC.RTCConfiguration
import cocoapods.WebRTC.RTCDataBuffer
import cocoapods.WebRTC.RTCDataChannel
import cocoapods.WebRTC.RTCDataChannelConfiguration
import cocoapods.WebRTC.RTCDataChannelDelegateProtocol
import cocoapods.WebRTC.RTCDataChannelState
import cocoapods.WebRTC.RTCIceCandidate
import cocoapods.WebRTC.RTCIceConnectionState
import cocoapods.WebRTC.RTCIceGatheringState
import cocoapods.WebRTC.RTCIceServer
import cocoapods.WebRTC.RTCInitializeSSL
import cocoapods.WebRTC.RTCMediaConstraints
import cocoapods.WebRTC.RTCMediaStream
import cocoapods.WebRTC.RTCPeerConnection
import cocoapods.WebRTC.RTCPeerConnectionDelegateProtocol
import cocoapods.WebRTC.RTCPeerConnectionFactory
import cocoapods.WebRTC.RTCSdpType
import cocoapods.WebRTC.RTCSessionDescription
import cocoapods.WebRTC.RTCShutdownInternalTracer
import cocoapods.WebRTC.RTCSignalingState
import cocoapods.WebRTC.dataChannelForLabel
import com.baccaro.lucas.conversation.presentation.FullServerEvent
import com.baccaro.lucas.platform.BaseInterviewWebRTCClient
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionPortOverrideNone
import platform.AVFAudio.AVAudioSessionPortOverrideSpeaker
import platform.AVFAudio.setActive
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.torchMode
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.darwin.NSObject
import kotlin.Any
import kotlin.Boolean
import kotlin.Exception
import kotlin.OptIn
import kotlin.String
import kotlin.Unit
import kotlin.apply
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.let

// Clase concreta de iOS
@OptIn(ExperimentalForeignApi::class)
class IosInterviewWebRTCClient(
    context: Any,
    ephemeralKey: String,
    onServerEvent: (FullServerEvent) -> Unit
) : BaseInterviewWebRTCClient(ephemeralKey, onServerEvent) {

    private var peerConnection: RTCPeerConnection? = null
    private var dataChannel: RTCDataChannel? = null
    private var peerConnectionFactory: RTCPeerConnectionFactory? = null

    private val peerDelegate = PeerConnectionDelegate()
    private val dataChannelDelegate = DataChannelDelegate { jsonString ->
        onServerEvent(jsonSerializer.decodeFromString(jsonString))
    }

    override fun setupPeerConnection() {
        RTCInitializeSSL()
        peerConnectionFactory = RTCPeerConnectionFactory()

        val iceServer = RTCIceServer(uRLStrings = listOf("stun:stun.l.google.com:19302"))
        val rtcConfig = RTCConfiguration().apply { iceServers = listOf(iceServer) }
        val constraints = RTCMediaConstraints(mandatoryConstraints = null, optionalConstraints = null)

        peerConnection = peerConnectionFactory!!.peerConnectionWithConfiguration(rtcConfig, constraints, peerDelegate) ?: throw Exception("PeerConnection es nulo en iOS")

        val dcInit = RTCDataChannelConfiguration()
        dataChannel = peerConnection?.dataChannelForLabel("oai-events", dcInit)
        dataChannel?.delegate = dataChannelDelegate

        val audioSource = peerConnectionFactory!!.audioSourceWithConstraints(null)
        val audioTrack = peerConnectionFactory!!.audioTrackWithSource(audioSource, "audio0")
        val localStream = peerConnectionFactory!!.mediaStreamWithStreamId("stream0")
        localStream.addAudioTrack(audioTrack)
        peerConnection?.addStream(localStream)
    }

    override suspend fun createOfferAndSetLocalDescription(): String {
        val offer = peerConnection!!.createOffer()
        peerConnection!!.setLocalDescription(offer)
        return offer.sdp
    }

    override suspend fun setRemoteDescription(sdp: String) {
        val answer = RTCSessionDescription(RTCSdpType.RTCSdpTypeAnswer, sdp)
        peerConnection!!.setRemoteDescription(answer)
    }

    override fun sendDataOnChannel(data: String) {
        if (dataChannel?.readyState != RTCDataChannelState.RTCDataChannelStateOpen) return
        val nsString = NSString.create(string = data)
        val buffer = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return
        dataChannel?.sendData(RTCDataBuffer(buffer, false))
    }

    override fun performCleanup() {
        dataChannel?.close()
        peerConnection?.close()
        RTCShutdownInternalTracer()
        peerConnectionFactory = null
        dataChannel = null
        peerConnection = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PeerConnectionDelegate : NSObject(), RTCPeerConnectionDelegateProtocol {
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceConnectionState: RTCIceConnectionState
    ) {
        println("KMP-WebRTC-iOS: ICE Connection State -> ${didChangeIceConnectionState.name}")
    }

    // ¡AQUÍ VA LA ANOTACIÓN!
    @ObjCSignatureOverride
    override fun peerConnection(peerConnection: RTCPeerConnection, didAddStream: RTCMediaStream) {
        println("KMP-WebRTC-iOS: Stream de audio remoto añadido con ID: ${didAddStream.streamId}")
        didAddStream.audioTracks.firstOrNull()?.let { track ->
            (track as? RTCAudioTrack)?.isEnabled = true
            println("KMP-WebRTC-iOS: Pista de audio remota habilitada.")
        }
    }

    // --- MÉTODOS OBLIGATORIOS PARA CUMPLIR EL PROTOCOLO ---

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeSignalingState: RTCSignalingState
    ) {
        println("KMP-WebRTC-iOS: Signaling State -> ${didChangeSignalingState.name}")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceGatheringState: RTCIceGatheringState
    ) {
        println("KMP-WebRTC-iOS: ICE Gathering State -> ${didChangeIceGatheringState.name}")
    }

    // ¡Y AQUÍ TAMBIÉN VA LA ANOTACIÓN!
    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveStream: RTCMediaStream
    ) {
        println("KMP-WebRTC-iOS: Stream remoto eliminado con ID: ${didRemoveStream.streamId}")
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
        println("KMP-WebRTC-iOS: Renegociación necesaria (acción no implementada).")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: RTCIceCandidate
    ) {
        // No se requiere acción.
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didOpenDataChannel: RTCDataChannel
    ) {
        println("KMP-WebRTC-iOS: El par remoto abrió un DataChannel: ${didOpenDataChannel.label}")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveIceCandidates: List<*>
    ) {
        // No se requiere acción.
    }
}


// Extensiones para manejar las APIs de iOS con coroutines (wrappers de completion handlers)
@OptIn(ExperimentalForeignApi::class)
private suspend fun RTCPeerConnection.createOffer(): RTCSessionDescription = suspendCancellableCoroutine { continuation ->
    val constraints = RTCMediaConstraints(mandatoryConstraints = null, optionalConstraints = null)
    offerForConstraints(constraints) { sdp, error ->
        if (sdp != null) {
            continuation.resume(sdp)
        } else {
            continuation.resumeWithException(Exception("Error creando oferta SDP: ${error?.localizedDescription}"))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun RTCPeerConnection.setLocalDescription(sdp: RTCSessionDescription): Unit = suspendCancellableCoroutine { continuation ->
    setLocalDescription(sdp) { error ->
        if (error == null) {
            continuation.resume(Unit)
        } else {
            continuation.resumeWithException(Exception("Error estableciendo descripción local: ${error.localizedDescription}"))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun RTCPeerConnection.setRemoteDescription(sdp: RTCSessionDescription): Unit = suspendCancellableCoroutine { continuation ->
    setRemoteDescription(sdp) { error ->
        if (error == null) {
            continuation.resume(Unit)
        } else {
            continuation.resumeWithException(Exception("Error estableciendo descripción remota: ${error.localizedDescription}"))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class DataChannelDelegate(val onMessage: (String) -> Unit) : NSObject(),
    RTCDataChannelDelegateProtocol {
    override fun dataChannel(dataChannel: RTCDataChannel, didReceiveMessageWithBuffer: RTCDataBuffer) {
        val data = didReceiveMessageWithBuffer.data
        val jsonString = NSString.create(data = data, encoding = NSUTF8StringEncoding) as String?
        jsonString?.let { onMessage(it) }
    }
    override fun dataChannelDidChangeState(dataChannel: RTCDataChannel) {
        println("KMP-WebRTC-iOS DataChannel state changed: ${dataChannel.readyState}")
    }
}