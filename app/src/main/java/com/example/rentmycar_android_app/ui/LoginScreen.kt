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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmycar_android_app.R
import com.example.rentmycar_android_app.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val service = remember { ApiClient.instance.create(AuthService::class.java) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3ECFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(30.dp)
        ) {
            Text(
                text = stringResource(R.string.login_brand),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Button(
                onClick = {
                    loading = true
                    errorMessage = null

                    scope.launch {
                        try {
                            val request = LoginRequest(email, password)
                            val response = service.login(request)

                            if (response.isSuccessful && response.body() != null) {
                                val authResponse = response.body()!!
                                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                sharedPrefs.edit()
                                    .putString("jwt_token", authResponse.token)
                                    .putString("username", authResponse.username)
                                    .apply()
                                onLoginSuccess(authResponse.username)
                            } else {
                                errorMessage = context.getString(R.string.incorrect_credentials)
                            }
                        } catch (e: Exception) {
                            errorMessage = context.getString(R.string.network_error, e.message ?: "Unknown")
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading
            ) {
                Text(stringResource(R.string.btn_login))
            }

            TextButton(onClick = onNavigateToForgotPassword) {
                Text(stringResource(R.string.forgot_password))
            }

            TextButton(onClick = onNavigateToRegister) {
                Text(stringResource(R.string.no_account_register))
            }

            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}