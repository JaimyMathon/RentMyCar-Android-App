package com.example.rentmycar_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rentmycar_android_app.ui.LoginScreen
import com.example.rentmycar_android_app.ui.RegisterScreen

sealed class Screen(val route: String) {
    object Login: Screen("login")
    object Register: Screen("register")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { /* TODO: ga naar volgende scherm */ },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}
