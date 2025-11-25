package com.example.rentmycar_android_app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val username = sharedPrefs.getString("username", "Onbekend")
    val token = sharedPrefs.getString("jwt_token", "")

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFE3ECFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welkom, $username!", fontSize = 28.sp)
            Spacer(Modifier.height(20.dp))
            Text("Je JWT token:", fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            Text(token ?: "Geen token gevonden", fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}
