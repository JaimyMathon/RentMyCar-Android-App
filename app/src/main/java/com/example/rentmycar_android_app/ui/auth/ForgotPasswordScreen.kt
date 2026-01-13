package com.example.rentmycar_android_app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmycar_android_app.core.network.ApiClient
import com.example.rentmycar_android_app.auth.AuthService
import com.example.rentmycar_android_app.auth.ResetPasswordRequest
import com.example.rentmycar_android_app.core.network.SimpleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val service = ApiClient.instance.create(AuthService::class.java)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3ECFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Wachtwoord wijzigen",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nieuw wachtwoord") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Bevestig wachtwoord") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Button(
                onClick = {
                    errorMessage = null
                    successMessage = null
                    loading = true

                    if (email.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                        loading = false
                        errorMessage = "Vul alle velden in"
                        return@Button
                    }

                    if (newPassword != confirmPassword) {
                        loading = false
                        errorMessage = "Wachtwoorden komen niet overeen"
                        return@Button
                    }

                    val request = ResetPasswordRequest(
                        email = email.trim(),
                        newPassword = newPassword
                    )

                    service.resetPassword(request).enqueue(object : Callback<SimpleResponse> {

                        override fun onResponse(
                            call: Call<SimpleResponse>,
                            response: Response<SimpleResponse>
                        ) {
                            loading = false
                            if (response.isSuccessful && response.body()?.isSuccess == true) {
                                successMessage = response.body()?.message
                            } else {
                                errorMessage = response.body()?.message ?: "Fout bij resetten"
                            }
                        }

                        override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                            loading = false
                            errorMessage = "Netwerkfout: ${t.message}"
                        }
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading
            ) {
                Text("Wachtwoord wijzigen")
            }

            TextButton(
                onClick = onNavigateBackToLogin,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Terug naar inloggen")
            }

            errorMessage?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 12.dp))
            }

            successMessage?.let {
                Text(text = it, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}
