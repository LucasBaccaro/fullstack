package com.baccaro.lucas.home.presentation

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
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateDetail: (String) -> Unit,viewModel: TopicsViewModel, modifier: Modifier = Modifier) {
    val topicsState by viewModel.topicsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getTopics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Temas disponibles") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: menú o logout */ }) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = topicsState) {
                is TopicsState.Loading -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator()
                }

                is TopicsState.Error -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }

                is TopicsState.Success -> {
                    if (state.topics.isEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "No hay temas disponibles.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            state.topics.forEach { topic ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onNavigateDetail(topic.prompt_context) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            topic.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        topic.description?.let {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(it, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
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
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    Surface(
        color = color,
        shape = RoundedCornerShape(50),
        tonalElevation = 2.dp,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = "Dificultad: ${level ?: "-"}",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
