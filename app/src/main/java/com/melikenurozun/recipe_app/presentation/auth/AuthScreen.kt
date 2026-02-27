package com.melikenurozun.recipe_app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onNavigateToHome()
        }
    }

    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Background Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.40f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Mystic Recipes",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cook. Share. Discover.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Form Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.isLogin) "Welcome Back" else "Create Account",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    if (!uiState.isLogin) {
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.onEvent(AuthEvent.UsernameChanged(it)) },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onEvent(AuthEvent.PasswordChanged(it)) },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var showForgotPasswordDialog by remember { mutableStateOf(false) }
                    var resetEmail by remember { mutableStateOf("") }

                    if (showForgotPasswordDialog) {
                        if (uiState.resetPasswordSuccess) {
                            AlertDialog(
                                onDismissRequest = { 
                                    viewModel.onEvent(AuthEvent.ClearResetSuccess)
                                    showForgotPasswordDialog = false 
                                },
                                title = { Text("Email Sent") },
                                text = { Text("Check your inbox for password reset instructions.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.onEvent(AuthEvent.ClearResetSuccess)
                                            showForgotPasswordDialog = false
                                        }
                                    ) { Text("OK") }
                                }
                            )
                        } else {
                            AlertDialog(
                                onDismissRequest = { showForgotPasswordDialog = false },
                                title = { Text("Reset Password") },
                                text = {
                                    Column {
                                        Text("Enter your email address to receive reset instructions.")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = resetEmail,
                                            onValueChange = { resetEmail = it },
                                            label = { Text("Email") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { viewModel.onEvent(AuthEvent.ResetPassword(resetEmail)) },
                                        enabled = !uiState.isLoading && resetEmail.isNotBlank()
                                    ) {
                                        if (uiState.isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                                        } else {
                                            Text("Send")
                                        }
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.rememberMe,
                                onCheckedChange = { viewModel.onEvent(AuthEvent.ToggleRememberMe) }
                            )
                            Text(text = "Remember me", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { 
                                resetEmail = uiState.email 
                                showForgotPasswordDialog = true 
                            }
                        ) {
                            Text(text = "Forgot Password?", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.onEvent(AuthEvent.Submit) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (uiState.isLogin) "Login" else "Sign Up", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { viewModel.onEvent(AuthEvent.ToggleAuthMode) }
                    ) {
                        Text(
                            if (uiState.isLogin) "Don't have an account? Sign Up" 
                            else "Already have an account? Login"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.onEvent(AuthEvent.GuestLogin) }
            ) {
                Text(
                    text = "I came to look",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
