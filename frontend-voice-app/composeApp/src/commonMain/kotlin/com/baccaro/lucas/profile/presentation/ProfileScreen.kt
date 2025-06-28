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
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = profileState) {
                    is ProfileState.Loading -> {
                        Spacer(modifier = Modifier.height(32.dp))
                        CircularProgressIndicator()
                    }
                    is ProfileState.Error -> {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                    is ProfileState.Success -> {
                        val profile = state.profile
                        if (!showEdit) {
                            Card(
                                modifier = Modifier.fillMaxWidth(0.98f),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("${profile.name ?: "Sin nombre"}", style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${profile.id}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nivel de inglés: ${profile.english_level ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        editableProfile = profile
                                        showEdit = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Editar perfil")
                                    }
                                }
                            }
                        } else {
                            var name by remember { mutableStateOf(profile.name ?: "") }
                            var englishLevel by remember { mutableStateOf(profile.english_level ?: "") }
                            Card(
                                modifier = Modifier.fillMaxWidth(0.98f),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Nombre") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = englishLevel,
                                        onValueChange = { englishLevel = it },
                                        label = { Text("Nivel de inglés") },
                                        leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row {
                                        Button(onClick = {
                                            viewModel.updateProfile(profile.copy(name = name, english_level = englishLevel))
                                            showEdit = false
                                            showSuccess = true
                                        }) {
                                            Text("Guardar")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(onClick = { showEdit = false }) {
                                            Text("Cancelar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is ProfileState.Updated -> {
                        if (showSuccess) {
                            Snackbar(
                                modifier = Modifier.padding(8.dp),
                                action = {
                                    TextButton(onClick = { showSuccess = false }) {
                                        Text("OK")
                                    }
                                }
                            ) {
                                Text("Perfil actualizado correctamente")
                            }
                        }
                        LaunchedEffect(Unit) {
                            viewModel.getProfile()
                        }
                    }
                    else -> {}
                }
            }
        }
    }
} 