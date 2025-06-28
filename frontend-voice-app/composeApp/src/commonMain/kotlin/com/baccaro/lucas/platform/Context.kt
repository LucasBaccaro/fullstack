package com.baccaro.lucas.platform

import androidx.compose.runtime.Composable
import com.baccaro.lucas.conversation.presentation.FullServerEvent

@Composable
expect fun getPlatformContext(): Any

@Composable
expect fun PermissionHandler(onResult: (Boolean) -> Unit)

expect fun createRtcClient(
    context: Any,
    ephemeralKey: String,
    onServerEvent: (FullServerEvent) -> Unit
): BaseInterviewWebRTCClient
