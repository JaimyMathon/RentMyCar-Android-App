// Bestandsnaam: app/src/main/java/com/example/rentmycar_android_app/ui/LoginScreen.kt

package com.example.rentmycar_android_app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

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
        modifier = Modifier.fillMaxSize().background(Color(0xFFE3ECFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(30.dp)
        ) {
            Text(
                "Rent My Car",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Wachtwoord") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                Text("Inloggen")
            }

            TextButton(onClick = onNavigateToForgotPassword) {
                Text("Wachtwoord vergeten?")
            }

            TextButton(onClick = onNavigateToRegister) {
                Text("Nog geen account? Registreer hier")
            }

            uiState.error?.let {
                Text(
                    it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}