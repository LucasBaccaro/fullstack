package com.baccaro.lucas.profile.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccaro.lucas.progress.model.ProgressReport
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val viewModel = koinInject<ProfileViewModel>()
    val progressState by viewModel.progressState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        containerColor = Color(0xFFF6F4F9),
        topBar = {
            TopAppBar(
                title = { Text("Mi Progreso", fontSize = 32.sp, color = Color(0xFF2D2A3A)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = progressState) {
                is ProgressHistoryState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF7B5EA7))
                }
                is ProgressHistoryState.Error -> {
                    Text(
                        text = "Error al cargar el progreso: ${state.message}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProgressHistoryState.Success -> {
                    if (state.history.isEmpty()) {
                        EmptyProgressState()
                    } else {
                        ProgressDashboard(history = state.history)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProgressState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)
    ) {
        Text(
            text = "Tu Aventura de Inglés Comienza Aquí",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF2D2A3A),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Completa tu primera sesión de práctica para ver tu historial y un análisis detallado de tu evolución.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { /* TODO: Navegar a la pantalla de iniciar práctica */ }) {
            Text("Comenzar mi Primera Práctica")
        }
    }
}

@Composable
fun ProgressDashboard(history: List<ProgressReport>) {
    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProgressSummaryCard(history = history)
        }
        item {
            Text("Historial de Sesiones", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
        }
        items(history) { report ->
            ProgressHistoryItem(report = report)
        }
    }
}

@Composable
fun ProgressSummaryCard(history: List<ProgressReport>) {
    val latestReport = history.first()
    val totalMinutes = history.sumOf { it.duration_minutes } // Corregido para usar el campo del modelo

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Nivel Actual (Sugerido): ${latestReport.suggested_level}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF7B5EA7)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${history.size}", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "Sesiones")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$totalMinutes", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "Minutos")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressHistoryItem(report: ProgressReport) {
    var expanded by remember { mutableStateOf(false) }

    Card(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Solución KMP-friendly para la fecha
            val formattedDate = report.session_date.substringBefore("T")
            Text(text = formattedDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = report.ai_summary, maxLines = if (expanded) Int.MAX_VALUE else 2, style = MaterialTheme.typography.bodyMedium)
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    if (report.grammar_points.isNotEmpty()) {
                        Text("Puntos de Gramática:", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        report.grammar_points.forEach { point ->
                            Text("• ${point.point}: ${point.examples.joinToString()}")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    if (report.new_vocabulary.isNotEmpty()) {
                        Text("Nuevo Vocabulario:", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(report.new_vocabulary.joinToString(", "))
                    }
                }
            }
        }
    }
}