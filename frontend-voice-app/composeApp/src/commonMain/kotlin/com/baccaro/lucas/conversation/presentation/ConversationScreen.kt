package com.baccaro.lucas.conversation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baccaro.lucas.platform.PermissionHandler
import com.baccaro.lucas.platform.getPlatformContext
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(instructions: String) {
    println("ConversationScreen: instructions received -> $instructions")

    val viewModel = koinInject<ConversationViewModel>()
    val uiState = viewModel.uiState
    val platformContext = getPlatformContext()
    var permissionRequested by remember { mutableStateOf(false) }

    // Actualiza el prompt inicial y resetea el estado al entrar
    LaunchedEffect(instructions) {
        viewModel.onEvent(InterviewEvent.UpdateUserPrompt(instructions))
    }

    // Maneja el ciclo de vida de la vista
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(InterviewEvent.Reset) // Limpia al salir
        }
    }

    // Si se necesita pedir permiso, muestra el handler
    if (permissionRequested && !uiState.hasPermission) {
        PermissionHandler { isGranted ->
            permissionRequested = false // Resetea el trigger
            viewModel.onEvent(InterviewEvent.PermissionResult(isGranted))
            if (isGranted) {
                viewModel.onEvent(InterviewEvent.StartOrStopInterview(platformContext, instructions))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrevista de Simulación") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Contenido Superior ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.finalReportData == null) {
                        Text(uiState.statusMessage, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- NUEVO: Feedback visual de la IA hablando y subtítulos ---
                        when {
                            uiState.isAiSpeaking -> {
                                // Burbuja animada (placeholder simple)
                                AudioBubble()
                                if (uiState.aiResponseText.isNotBlank()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text(
                                            text = uiState.aiResponseText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            uiState.aiFinalTranscript.isNotBlank() -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text(
                                        text = uiState.aiFinalTranscript,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Contenido Inferior (Botón o Informe) ---
                if (uiState.finalReportData != null) {
                    FinalReportCard(uiState.finalReportData)
                } else {
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
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.connectionState != ConnectionState.RequestingToken && uiState.connectionState != ConnectionState.Connecting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.connectionState == ConnectionState.Connected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        val buttonText = when (uiState.connectionState) {
                            is ConnectionState.Connected -> "Finalizar Entrevista"
                            is ConnectionState.Connecting, ConnectionState.RequestingToken -> "Conectando..."
                            else -> "Comenzar Entrevista"
                        }
                        Text(buttonText)
                    }
                }
            }

            // --- Indicador de Carga (Centrado y Superpuesto) ---
            if (uiState.connectionState == ConnectionState.RequestingToken || uiState.connectionState == ConnectionState.Connecting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun FinalReportCard(report: FinalReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Informe Final", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Puntuación General: ${report.overall_score}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Resumen: ${report.summary}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Fortalezas: ${report.strengths.joinToString()}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Áreas de Mejora: ${report.areas_for_improvement.joinToString()}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nivel de Inglés: ${report.english_level}")
        }
    }
}

// --- NUEVO: Composable para la burbuja animada de audio ---
@Composable
fun AudioBubble() {
    // Placeholder simple: círculo animado (puedes reemplazar por Lottie o animación real)
    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "IA hablando",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

