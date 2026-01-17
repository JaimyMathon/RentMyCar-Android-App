package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: com.example.rentmycar_android_app.ui.login.LoginViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    // Handle login success
    LaunchedEffect(uiState.loginSuccess) {
        uiState.loginSuccess?.let { username ->
            onLoginSuccess(username)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Title
            Text(
                "Rent My Car",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "Log in om verder te gaan",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Login Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF6B6B6B))
                    .padding(20.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("E-mail") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0E9E9),
                            focusedContainerColor = Color(0xFFF0E9E9),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Wachtwoord") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0E9E9),
                            focusedContainerColor = Color(0xFFF0E9E9),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.login(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            "Inloggen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToForgotPassword) {
                Text(
                    "Wachtwoord vergeten?",
                    color = Color.DarkGray
                )
            }

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "Nog geen account? Registreer hier",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }

            uiState.error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        it,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}