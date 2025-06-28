package com.baccaro.lucas

import android.content.Context
import android.util.Log
import com.baccaro.lucas.conversation.presentation.FullServerEvent
import com.baccaro.lucas.platform.BaseInterviewWebRTCClient
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidInterviewWebRTCClient(
    context: Any,
    ephemeralKey: String,
    onServerEvent: (FullServerEvent) -> Unit
) : BaseInterviewWebRTCClient(ephemeralKey, onServerEvent) {

    private val androidContext = context as Context
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null

    private val peerConnectionObserver = object : PeerConnection.Observer {
        override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) { Log.d("KMP-WebRTC-Android", "ICE State: $newState") }
        override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) { (receiver?.track() as? AudioTrack)?.setEnabled(true) }
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
        override fun onDataChannel(dc: DataChannel?) {}
        override fun onIceConnectionReceivingChange(p0: Boolean) {}
        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
        override fun onIceCandidate(p0: IceCandidate?) {}
        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
        override fun onAddStream(p0: MediaStream?) {}
        override fun onRemoveStream(p0: MediaStream?) {}
        override fun onRenegotiationNeeded() {}
    }

    override fun setupPeerConnection() {
        val options = PeerConnectionFactory.InitializationOptions.builder(androidContext.applicationContext).createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, peerConnectionObserver) ?: throw Exception("PeerConnection es nulo")

        val dcInit = DataChannel.Init()
        dataChannel = peerConnection?.createDataChannel("oai-events", dcInit)
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onMessage(buffer: DataChannel.Buffer) {
                val jsonString = Charsets.UTF_8.decode(buffer.data).toString()
                onServerEvent(jsonSerializer.decodeFromString(jsonString))
            }
            override fun onStateChange() {}
            override fun onBufferedAmountChange(p0: Long) {}
        })

        val audioSource = peerConnectionFactory!!.createAudioSource(MediaConstraints())
        val audioTrack = peerConnectionFactory!!.createAudioTrack("audio0", audioSource)
        peerConnection?.addTrack(audioTrack, listOf("stream0"))
    }

    override suspend fun createOfferAndSetLocalDescription(): String {
        val offer = peerConnection!!.createOfferSdp()
        peerConnection!!.setLocalDescriptionSdp(offer)
        return offer.description
    }

    override suspend fun setRemoteDescription(sdp: String) {
        val answer = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        peerConnection!!.setRemoteDescriptionSdp(answer)
    }

    override fun sendDataOnChannel(data: String) {
        if (dataChannel?.state() != DataChannel.State.OPEN) return
        val buffer = DataChannel.Buffer(java.nio.ByteBuffer.wrap(data.toByteArray(Charsets.UTF_8)), false)
        dataChannel?.send(buffer)
    }

    override fun performCleanup() {
        dataChannel?.unregisterObserver()
        dataChannel?.close()
        peerConnection?.close()
        peerConnectionFactory?.dispose()
        dataChannel = null
        peerConnection = null
        peerConnectionFactory = null
    }
}

private open class SdpObserverAdapter : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}

suspend fun PeerConnection.createOfferSdp(): SessionDescription = suspendCancellableCoroutine { cont ->
    createOffer(object : SdpObserverAdapter() {
        override fun onCreateSuccess(sdp: SessionDescription?) { cont.resume(sdp!!) }
        override fun onCreateFailure(error: String?) { cont.resumeWithException(Exception("createOfferSdp falló: $error")) }
    }, MediaConstraints())
}

suspend fun PeerConnection.setLocalDescriptionSdp(sdp: SessionDescription) = suspendCancellableCoroutine<Unit> { cont ->
    setLocalDescription(object : SdpObserverAdapter() {
        override fun onSetSuccess() { cont.resume(Unit) }
        override fun onSetFailure(error: String?) { cont.resumeWithException(Exception("setLocalDescriptionSdp falló: $error")) }
    }, sdp)
}

suspend fun PeerConnection.setRemoteDescriptionSdp(sdp: SessionDescription) = suspendCancellableCoroutine<Unit> { cont ->
    setRemoteDescription(object : SdpObserverAdapter() {
        override fun onSetSuccess() { cont.resume(Unit) }
        override fun onSetFailure(error: String?) { cont.resumeWithException(Exception("setRemoteDescriptionSdp falló: $error")) }
    }, sdp)
}