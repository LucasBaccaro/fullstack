package com.baccaro.lucas.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.baccaro.lucas.IosInterviewWebRTCClient
import com.baccaro.lucas.conversation.presentation.FullServerEvent
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
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

