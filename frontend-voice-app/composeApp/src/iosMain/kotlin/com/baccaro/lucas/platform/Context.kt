package com.baccaro.lucas.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.baccaro.lucas.IosInterviewWebRTCClient
import com.baccaro.lucas.conversation.presentation.FullServerEvent
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionPortOverrideNone
import platform.AVFAudio.AVAudioSessionPortOverrideSpeaker
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.AVFAudio.setActive
import platform.darwin.NSObject

@Composable
actual fun getPlatformContext(): Any = NSObject()

@Composable
actual fun PermissionHandler(onResult: (Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        when (AVAudioSession.sharedInstance().recordPermission()) {
            AVAudioSessionRecordPermissionGranted -> {
                onResult(true)
            }
            AVAudioSessionRecordPermissionDenied -> {
                onResult(false)
            }
            AVAudioSessionRecordPermissionUndetermined -> {
                // Si aún no se ha preguntado, lo pedimos.
                AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                    onResult(granted)
                }
            }
        }
    }
}

actual fun createRtcClient(
    context: Any,
    ephemeralKey: String,
    onServerEvent: (FullServerEvent) -> Unit
): BaseInterviewWebRTCClient = IosInterviewWebRTCClient(context, ephemeralKey, onServerEvent)

actual class AudioHelper actual constructor(context: Any) {
    private val audioSession: AVAudioSession = AVAudioSession.sharedInstance()

    @OptIn(ExperimentalForeignApi::class)
    actual fun setSpeakerphoneOn(isOn: Boolean) {
        try {
            val categorySet = audioSession.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                error = null
            )
            if (!categorySet) {
                println("AudioHelper: No se pudo establecer la categoría de audio.")
                return
            }

            val port = if (isOn) AVAudioSessionPortOverrideSpeaker else AVAudioSessionPortOverrideNone
            val portSet = audioSession.overrideOutputAudioPort(port, error = null)
            if (!portSet) {
                println("AudioHelper: No se pudo cambiar la salida de audio.")
            }

            val activeSet = audioSession.setActive(true, error = null)
            if (!activeSet) {
                println("AudioHelper: No se pudo activar la sesión de audio.")
            }
            println("AudioHelper: Speakerphone is now ${if (isOn) "ON" else "OFF"}")
        } catch (e: Exception) {
            println("AudioHelper: Error configurando audio: ${e.message}")
        }
    }
}