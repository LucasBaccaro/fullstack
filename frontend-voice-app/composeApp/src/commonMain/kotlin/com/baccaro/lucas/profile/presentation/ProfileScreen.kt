package com.baccaro.lucas.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccaro.lucas.profile.domain.Profile
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val viewModel = koinInject<ProfileViewModel>()
    val profileState by viewModel.profileState.collectAsState()
    var editableProfile by remember { mutableStateOf<Profile?>(null) }
    var showEdit by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.getProfile() }

    Scaffold(
        containerColor = Color(0xFFF6F4F9),
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontSize = 32.sp, color = Color(0xFF2D2A3A)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = profileState) {
                    is ProfileState.Loading -> {
                        Spacer(modifier = Modifier.height(32.dp))
                        CircularProgressIndicator(color = Color(0xFF7B5EA7))
                    }
                    is ProfileState.Error -> {
                        Spacer(modifier = Modifier.height(32.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(state.message, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF7B5EA7))
                        )
                    }
                    is ProfileState.Success -> {
                        val profile = state.profile
                        if (!showEdit) {
                            Card(
                                modifier = Modifier.fillMaxWidth(0.98f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE3D6F7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF7B5EA7), modifier = Modifier.size(48.dp))
                                    }
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Text("${profile.name ?: "Sin nombre"}", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF2D2A3A)))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF7B5EA7))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${profile.id}", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF7B5EA7)))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF7B5EA7))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nivel de inglés: ${profile.english_level ?: "-"}", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF7B5EA7)))
                                    }
                                    Spacer(modifier = Modifier.height(22.dp))
                                    Button(onClick = {
                                        editableProfile = profile
                                        showEdit = true
                                    },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B5EA7)),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Editar perfil", color = Color.White)
                                    }
                                }
                            }
                        } else {
                            var name by remember { mutableStateOf(profile.name ?: "") }
                            var englishLevel by remember { mutableStateOf(profile.english_level ?: "") }
                            Card(
                                modifier = Modifier.fillMaxWidth(0.98f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Nombre", color = Color(0xFF7B5EA7)) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF7B5EA7)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF7B5EA7),
                                            unfocusedBorderColor = Color(0xFFE3D6F7)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = englishLevel,
                                        onValueChange = { englishLevel = it },
                                        label = { Text("Nivel de inglés", color = Color(0xFF7B5EA7)) },
                                        leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF7B5EA7)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF7B5EA7),
                                            unfocusedBorderColor = Color(0xFFE3D6F7)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(22.dp))
                                    Row {
                                        Button(onClick = {
                                            viewModel.updateProfile(profile.copy(name = name, english_level = englishLevel))
                                            showEdit = false
                                            showSuccess = true
                                        },
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B5EA7)),
                                            modifier = Modifier.height(44.dp)
                                        ) {
                                            Text("Guardar", color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(onClick = { showEdit = false }, shape = CircleShape) {
                                            Text("Cancelar", color = Color(0xFF7B5EA7))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is ProfileState.Updated -> {
                        if (showSuccess) {
                            AssistChip(
                                onClick = { showSuccess = false },
                                label = { Text("Perfil actualizado correctamente", color = Color.White) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF7B5EA7))
                            )
                        }
                        LaunchedEffect(Unit) { viewModel.getProfile() }
                    }
                    else -> {}
                }
            }
        }
    }
} 