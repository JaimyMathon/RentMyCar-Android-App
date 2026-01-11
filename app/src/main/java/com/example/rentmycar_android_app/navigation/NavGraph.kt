// Bestandsnaam: app/src/main/java/com/example/rentmycar_android_app/navigation/NavGraph.kt

package com.example.rentmycar_android_app.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rentmycar_android_app.ui.ForgotPasswordScreen
import com.example.rentmycar_android_app.ui.LoginScreen
import com.example.rentmycar_android_app.ui.RegisterScreen
import com.example.rentmycar_android_app.ui.HomeScreen
import com.example.rentmycar_android_app.ui.MapScreen
import com.example.rentmycar_android_app.ui.ProfileScreen
import androidx.core.content.edit
import com.example.rentmycar_android_app.ui.DrivingStatsScreen
import com.example.rentmycar_android_app.ui.DrivingTrackerScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")


    object Home : Screen("home")
    object Reservation : Screen("reservation")
    object Map : Screen("map")
    object Profile : Screen("profile")
    object DrivingTracker : Screen("driving_tracker")
    object DrivingStats : Screen("driving_stats")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },

                // gebruiker moet email invullen → we geven het email mee aan ResetPassword
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToReservation = {
                    navController.navigate(Screen.Reservation.route)
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToReservation = {
                    navController.navigate(Screen.Reservation.route)
                },
                onNavigateToCars = {},
                onNavigateToReservationsOverview = {},
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToDrivingTracker = {
                    navController.navigate(Screen.DrivingTracker.route)
                },
                onNavigateToDrivingStats = {
                    navController.navigate(Screen.DrivingStats.route)
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("jwt_token", "")

            ProfileScreen(
                token = token,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDrivingStats = {
                    navController.navigate(Screen.DrivingStats.route)
                },
                onLogout = {
                    sharedPrefs.edit { clear() }
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DrivingTracker.route) {
            DrivingTrackerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DrivingStats.route) {
            DrivingStatsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
