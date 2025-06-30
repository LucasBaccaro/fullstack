package com.baccaro.lucas.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateDetail: (String) -> Unit, viewModel: TopicsViewModel, modifier: Modifier = Modifier) {
    val topicsState by viewModel.topicsState.collectAsState()
    LaunchedEffect(Unit) { viewModel.getTopics() }
    Scaffold(
        containerColor = Color(0xFFF6F4F9),
        topBar = {
            TopAppBar(
                title = { Text("Temas", fontSize = 32.sp, color = Color(0xFF2D2A3A)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = topicsState) {
                is TopicsState.Loading -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator(color = Color(0xFF7B5EA7))
                }
                is TopicsState.Error -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(state.message, color = Color.White) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF7B5EA7))
                    )
                }
                is TopicsState.Success -> {
                    if (state.topics.isEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "No hay temas disponibles.",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF7B5EA7))
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            state.topics.forEach { topic ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { onNavigateDetail(topic.prompt_context) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Text(
                                            topic.title,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2D2A3A)
                                            )
                                        )
                                        topic.description?.let {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(it, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF7B5EA7)))
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            DifficultyChip(topic.difficulty_level)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun DifficultyChip(level: String?) {
    val color = when (level?.lowercase()) {
        "easy", "facil" -> Color(0xFFB9F6CA)
        "medium", "medio" -> Color(0xFFFFF59D)
        "hard", "dificil" -> Color(0xFFFF8A80)
        else -> Color(0xFFE3D6F7)
    }
    Surface(
        color = color,
        shape = RoundedCornerShape(50),
        tonalElevation = 0.dp,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = "Dificultad: ${level ?: "-"}",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF2D2A3A))
        )
    }
}
