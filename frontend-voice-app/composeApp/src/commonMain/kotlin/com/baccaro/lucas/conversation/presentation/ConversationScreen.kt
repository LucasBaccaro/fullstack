package com.baccaro.lucas.conversation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccaro.lucas.platform.PermissionHandler
import com.baccaro.lucas.platform.getPlatformContext
import org.koin.compose.koinInject
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.baccaro.lucas.progress.model.ProgressReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(instructions: String) {
    val viewModel = koinInject<ConversationViewModel>()
    val uiState = viewModel.uiState
    val platformContext = getPlatformContext()
    var permissionRequested by remember { mutableStateOf(false) }

    LaunchedEffect(instructions) {
        viewModel.onEvent(InterviewEvent.UpdateUserPrompt(instructions))
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(InterviewEvent.Reset) }
    }
    if (permissionRequested && !uiState.hasPermission) {
        PermissionHandler { isGranted ->
            permissionRequested = false
            viewModel.onEvent(InterviewEvent.PermissionResult(isGranted))
            if (isGranted) {
                viewModel.onEvent(InterviewEvent.StartOrStopInterview(platformContext, instructions))
            }
        }
    }
    Scaffold(
        containerColor = Color(0xFFF6F4F9),
        topBar = {
            TopAppBar(
                title = { Text("Simulación", fontSize = 32.sp, color = Color(0xFF2D2A3A)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.finalReportData == null) {
                    Text(
                        uiState.statusMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = Color(0xFF7B5EA7)),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    AnimatedVisibility(
                        visible = uiState.isAiSpeaking,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        AnimatedAudioBubble()
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    if (uiState.aiResponseText.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3D6F7)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Text(
                                text = uiState.aiResponseText,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF2D2A3A)),
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (uiState.aiFinalTranscript.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4F9)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Text(
                                text = uiState.aiFinalTranscript,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF2D2A3A)),
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    Button(
                        onClick = {
                            if (uiState.connectionState != ConnectionState.Connected) {
                                if (uiState.hasPermission) {
                                    viewModel.onEvent(InterviewEvent.StartOrStopInterview(platformContext, instructions))
                                } else {
                                    permissionRequested = true
                                }
                            } else {
                                viewModel.onEvent(InterviewEvent.StartOrStopInterview(platformContext, instructions))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape,
                        enabled = uiState.connectionState != ConnectionState.RequestingToken && uiState.connectionState != ConnectionState.Connecting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.connectionState == ConnectionState.Connected) Color(0xFFEF6C6C) else Color(0xFF7B5EA7)
                        )
                    ) {
                        val buttonText = when (uiState.connectionState) {
                            is ConnectionState.Connected -> "Finalizar"
                            is ConnectionState.Connecting, ConnectionState.RequestingToken -> "Conectando..."
                            else -> "Comenzar"
                        }
                        Text(buttonText, fontSize = 18.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            // Informe final fijo abajo
            if (uiState.finalReportData != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    FinalReportCard(uiState.finalReportData)
                }
            }
            if (uiState.connectionState == ConnectionState.RequestingToken || uiState.connectionState == ConnectionState.Connecting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF7B5EA7))
            }
        }
    }
}


@Composable
fun FinalReportCard(report: ProgressReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3D6F7)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Informe de Progreso", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF2D2A3A)))
            Spacer(modifier = Modifier.height(16.dp))

            // Resumen
            Text("Resumen de la Sesión", style = MaterialTheme.typography.titleMedium)
            Text(report.ai_summary, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2D2A3A))
            Spacer(modifier = Modifier.height(16.dp))

            // Nivel Sugerido
            Text("Nivel Sugerido: ${report.suggested_level}", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF7B5EA7))
            Spacer(modifier = Modifier.height(16.dp))

            // Puntos de Gramática
            if (report.grammar_points.isNotEmpty()) {
                Text("Puntos de Gramática a Revisar", style = MaterialTheme.typography.titleMedium)
                report.grammar_points.forEach { point ->
                    Text("• ${point.point} (Estado: ${point.status})", style = MaterialTheme.typography.bodyMedium)
                    point.examples.forEach { example ->
                        Text("  - Ejemplo: \"$example\"", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Nuevo Vocabulario
            if (report.new_vocabulary.isNotEmpty()) {
                Text("Nuevo Vocabulario", style = MaterialTheme.typography.titleMedium)
                Text(report.new_vocabulary.joinToString(), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Temas Discutidos
            if (report.topics_discussed.isNotEmpty()) {
                Text("Temas Discutidos", style = MaterialTheme.typography.titleMedium)
                Text(report.topics_discussed.joinToString(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun AnimatedAudioBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "audio-bubble")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )
    Box(
        modifier = Modifier
            .size((56 * scale).dp)
            .clip(CircleShape)
            .background(Color(0xFF7B5EA7).copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "IA hablando",
            tint = Color(0xFF7B5EA7),
            modifier = Modifier.size(32.dp)
        )
    }
}

