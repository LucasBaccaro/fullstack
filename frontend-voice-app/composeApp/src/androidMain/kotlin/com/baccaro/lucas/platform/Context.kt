package com.baccaro.lucas.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.baccaro.lucas.AndroidInterviewWebRTCClient
import com.baccaro.lucas.conversation.presentation.FullServerEvent

@Composable
actual fun getPlatformContext(): Any {
    return LocalContext.current
}

@Composable
actual fun PermissionHandler(onResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    // Usamos el contrato para múltiples permisos
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        // El resultado es un mapa de Permiso -> Booleano.
        // Solo consideramos éxito si TODOS los permisos fueron concedidos.
        val allGranted = permissionsMap.values.all { it }
        onResult(allGranted)
    }

    LaunchedEffect(Unit) {
        val allPermissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            onResult(true)
        } else {
            launcher.launch(permissions)
        }
    }
}

actual fun createRtcClient(
    context: Any,
    ephemeralKey: String,
    onServerEvent: (FullServerEvent) -> Unit
): BaseInterviewWebRTCClient = AndroidInterviewWebRTCClient(context, ephemeralKey, onServerEvent)

actual class AudioHelper actual constructor(context: Any) {
    private val audioManager =
        (context as Context).getSystemService(Context.AUDIO_SERVICE) as AudioManager

    actual fun setSpeakerphoneOn(isOn: Boolean) {
        audioManager.isSpeakerphoneOn = isOn
        if (isOn) {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } else {
            // Al apagarlo, volver al modo normal es una buena práctica
            audioManager.mode = AudioManager.MODE_NORMAL
        }
        Log.d("AudioHelper", "Speakerphone is now ${if (isOn) "ON" else "OFF"}")
    }
}