package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Title
        Text(
            "Log In",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Hallo welkom terug, we hebben je gemist",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Email label
        Text(
            "Email",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("voorbeeld@gmail.com", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password label
        Text(
            "Wachtwoord",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Wachtwoord", color = Color.Gray) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot password link
        Text(
            "Wachtwoord vergeten",
            fontSize = 14.sp,
            color = Color.Gray,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onNavigateToForgotPassword() }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Error message
        uiState.error?.let {
            Text(
                it,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Login button
        Button(
            onClick = {
                viewModel.login(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6B6B6B)
            ),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    "Log In",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Heb je nog geen account? ",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                "Registreer hier",
                fontSize = 14.sp,
                color = Color.Gray,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}