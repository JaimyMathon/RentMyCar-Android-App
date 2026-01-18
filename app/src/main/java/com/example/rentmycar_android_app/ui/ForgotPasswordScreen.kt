package com.example.rentmycar_android_app.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.rentmycar_android_app.R
import com.example.rentmycar_android_app.network.ApiClient
import com.example.rentmycar_android_app.network.AuthService
import com.example.rentmycar_android_app.network.ResetPasswordRequest
import com.example.rentmycar_android_app.network.SimpleResponse

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

    val context = LocalContext.current
    val fillAllFieldsError = stringResource(R.string.fill_all_fields)
    val passwordsNotMatchError = stringResource(R.string.passwords_not_match)
    val resetError = stringResource(R.string.reset_error)
    val networkErrorFormat: (String) -> String = { message ->
        context.getString(R.string.network_error, message)
    }

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
                text = stringResource(R.string.change_password),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(stringResource(R.string.new_password)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
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
                        errorMessage = fillAllFieldsError
                        return@Button
                    }

                    if (newPassword != confirmPassword) {
                        loading = false
                        errorMessage = passwordsNotMatchError
                        return@Button
                    }

                    val request = ResetPasswordRequest(
                        email = email.trim(),
                        newPassword = newPassword
                    )

                    service.resetPassword(request).enqueue(object : retrofit2.Callback<SimpleResponse> {

                        override fun onResponse(
                            call: retrofit2.Call<SimpleResponse>,
                            response: retrofit2.Response<SimpleResponse>
                        ) {
                            loading = false
                            if (response.isSuccessful && response.body()?.isSuccess == true) {
                                successMessage = response.body()?.message
                            } else {
                                errorMessage = response.body()?.message ?: resetError
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<SimpleResponse>, t: Throwable) {
                            loading = false
                            errorMessage = networkErrorFormat(t.message ?: "")
                        }
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading
            ) {
                Text(stringResource(R.string.change_password_button))
            }

            TextButton(
                onClick = onNavigateBackToLogin,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.back_to_login))
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
