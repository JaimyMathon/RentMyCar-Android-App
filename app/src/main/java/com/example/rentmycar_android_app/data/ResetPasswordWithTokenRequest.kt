// Bestandsnaam: app/src/main/java/com/example/rentmycar_android_app/data/ResetPasswordWithTokenRequest.kt

package com.example.rentmycar_android_app.data

data class ResetPasswordWithTokenRequest(
    val token: String,
    val newPassword: String
)