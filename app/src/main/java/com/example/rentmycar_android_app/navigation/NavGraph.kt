// Bestandsnaam: app/src/main/java/com/example/rentmycar_android_app/navigation/NavGraph.kt

package com.example.rentmycar_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rentmycar_android_app.ui.LoginScreen
import com.example.rentmycar_android_app.ui.RegisterScreen
import com.example.rentmycar_android_app.ui.HomeScreen
import com.example.rentmycar_android_app.ui.ResetPasswordScreen
import com.example.rentmycar_android_app.ui.MapScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ResetPassword : Screen("reset-password/{token}") {
        fun createRoute(token: String) = "reset-password/$token"

    }    object Home : Screen("home")
    object Home : Screen("home")
    object Map : Screen("map")
}

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ResetPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen()
        }

        composable(
            route = "reset-password/{token}"
        ) { backStackEntry ->

            val token = backStackEntry.arguments?.getString("token") ?: ""

            ResetPasswordScreen(
                tokenFromLink = token,
                onSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}