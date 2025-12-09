package com.example.rentmycar_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rentmycar_android_app.ui.LoginScreen
import com.example.rentmycar_android_app.ui.RegisterScreen
import com.example.rentmycar_android_app.ui.HomeScreen
import com.example.rentmycar_android_app.ui.ReservationScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Reservation : Screen("reservation")
}

@Composable
fun NavGraph(navController: NavHostController) {

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
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
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

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToReservation = {
                    navController.navigate(Screen.Reservation.route)
                }
                // onNavigateToCars, onNavigateToReservationsOverview, onNavigateToProfile
                // laten we voorlopig op de default {} staan
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToReservation = {
                    navController.navigate(Screen.Reservation.route)
                },
                onNavigateToCars = { /* later: navController.navigate(Screen.Cars.route) */ },
                onNavigateToReservationsOverview = { /* navController.navigate(...) */ },
                onNavigateToProfile = { /* navController.navigate(Screen.Profile.route) */ }
            )
        }

        composable(Screen.Reservation.route) {
            ReservationScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { fromDate, toDate, kms ->
                    // hier later: ReservationDto maken + ReservationService.addReservation aanroepen
                }
            )
        }
    }
}