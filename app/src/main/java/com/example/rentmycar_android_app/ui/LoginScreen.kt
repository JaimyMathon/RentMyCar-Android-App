package com.example.rentmycar_android_app.ui

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import com.example.rentmycar_android_app.network.*
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val service = ApiClient.instance.create(AuthService::class.java)

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
                    loading = true
                    errorMessage = null
                    val request = LoginRequest(email, password)

                    val service = ApiClient.instance.create(AuthService::class.java)

                    service.login(request).enqueue(object: retrofit2.Callback<AuthResponse> {
                        override fun onResponse(call: retrofit2.Call<AuthResponse>, response: retrofit2.Response<AuthResponse>) {
                            loading = false
                            if (response.isSuccessful && response.body() != null) {
                                val authResponse = response.body()!!
                                // Token opslaan
                                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                sharedPrefs.edit().putString("jwt_token", authResponse.token).apply()

                                // Optioneel: gebruikersnaam opslaan
                                sharedPrefs.edit().putString("username", authResponse.username).apply()
                                onLoginSuccess(authResponse.username)
                            } else {
                                errorMessage = "E-mail of wachtwoord onjuist"
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<AuthResponse>, t: Throwable) {
                            loading = false
                            errorMessage = "Netwerkfout: ${t.message}"
                        }
                    })
                },
                modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading
            ) { Text("Inloggen") }

            TextButton(onClick = onNavigateToRegister) {
                Text("Nog geen account? Registreer hier")
            }

            errorMessage?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp)) }
            if (loading) CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
