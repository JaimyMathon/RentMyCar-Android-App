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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.rentmycar_android_app.R
import com.example.rentmycar_android_app.network.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
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
                stringResource(R.string.register_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            Button(
                onClick = {
                    loading = true
                    errorMessage = null
                    val request = RegisterRequest(name, email, password)

                    service.register(request).enqueue(object: retrofit2.Callback<AuthResponse> {
                        override fun onResponse(call: retrofit2.Call<AuthResponse>, response: retrofit2.Response<AuthResponse>) {
                            loading = false
                            if (response.isSuccessful && response.body() != null) {
                                val authResponse = response.body()!!

                                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                sharedPrefs.edit().putString("jwt_token", authResponse.token).apply()
                                sharedPrefs.edit().putString("username", authResponse.username).apply()
                                onRegisterSuccess()
                            } else {
                                errorMessage = "Registratie mislukt"
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
            ) { Text(stringResource(R.string.register_title)) }

            TextButton(onClick = onNavigateToLogin) {
                Text(stringResource(R.string.already_have_account))
            }

            errorMessage?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp)) }
            if (loading) CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

    }
}
