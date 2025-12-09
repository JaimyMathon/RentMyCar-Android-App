package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentmycar_android_app.network.ApiClient
import com.example.rentmycar_android_app.api.ApiService
import com.example.rentmycar_android_app.viewmodels.ResetPasswordViewModel
import com.example.rentmycar_android_app.viewmodels.ResetPasswordViewModelFactory

@Composable
fun ResetPasswordScreen(
    tokenFromLink: String,
    onSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val apiService = ApiClient.instance.create(ApiService::class.java)

    val viewModel: ResetPasswordViewModel = viewModel(
        factory = ResetPasswordViewModelFactory(apiService, tokenFromLink)
    )


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Wachtwoord Resetten",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Token is automatisch gegenereerd (zie Logcat)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = viewModel.token,
                    onValueChange = { viewModel.token = it },
                    label = { Text("Reset Token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.newPassword,
                    onValueChange = { viewModel.newPassword = it },
                    label = { Text("Nieuw Wachtwoord") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.confirmPassword,
                    onValueChange = { viewModel.confirmPassword = it },
                    label = { Text("Bevestig Wachtwoord") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.resetPassword(onSuccess) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Wachtwoord Wijzigen")
                    }
                }

                if (viewModel.message.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        viewModel.message,
                        color = if (viewModel.message.contains("succesvol"))
                            Color(0xFF4CAF50) else Color.Red,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onBackToLogin) {
                    Text("Terug naar login")
                }
            }
        }
    }
}
