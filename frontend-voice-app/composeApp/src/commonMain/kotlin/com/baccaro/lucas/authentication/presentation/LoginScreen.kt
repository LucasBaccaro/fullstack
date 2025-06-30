package com.baccaro.lucas.authentication.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AuthViewModel, onLoginSuccess: () -> Unit, onNavigateToSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    DisposableEffect(Unit) {
        onDispose { viewModel.reset() }
    }
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onLoginSuccess()
    }

    Scaffold(
        containerColor = Color(0xFFF6F4F9),
        topBar = {
            /*TopAppBar(
                title = { Text("Hoy", fontSize = 32.sp, color = Color(0xFF2D2A3A)) },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF2D2A3A),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3D6F7))
                            .padding(4.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )*/
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3D6F7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF7B5EA7), modifier = Modifier.size(44.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color(0xFF7B5EA7)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF7B5EA7)) },
                    enabled = !isLoading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7B5EA7),
                        unfocusedBorderColor = Color(0xFFE3D6F7)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = Color(0xFF7B5EA7)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF7B5EA7)) },
                    enabled = !isLoading,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7B5EA7),
                        unfocusedBorderColor = Color(0xFFE3D6F7)
                    )
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { viewModel.signIn(email, password) },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B5EA7))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Iniciar sesión", fontSize = 18.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onNavigateToSignUp, modifier = Modifier.fillMaxWidth()) {
                    Text("¿No tienes cuenta? Regístrate", color = Color(0xFF7B5EA7))
                }
                when (val state = authState) {
                    is AuthState.Error -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(state.message, color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF7B5EA7))
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
