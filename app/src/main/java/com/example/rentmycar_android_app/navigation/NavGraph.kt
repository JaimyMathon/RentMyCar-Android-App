package com.example.rentmycar_android_app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rentmycar_android_app.ui.*

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")

    data object CarDetail : Screen("car/{carId}") {
        fun createRoute(carId: String) = "car/${Uri.encode(carId)}"
    }

    data object Reservation : Screen("reservation/{carId}") {
        fun createRoute(carId: String) = "reservation/${Uri.encode(carId)}"
    }

    // ✅ PaymentReview krijgt ALLES wat jij nodig hebt
    data object PaymentReview : Screen("paymentReview/{carId}/{fromDate}/{toDate}/{kms}") {
        fun createRoute(carId: String, fromDate: String, toDate: String, kms: String): String {
            return "paymentReview/${Uri.encode(carId)}/${Uri.encode(fromDate)}/${Uri.encode(toDate)}/${Uri.encode(kms)}"
        }
    }

    data object PaymentMethod : Screen("paymentMethod")
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
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
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
            HomeScreen(
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetail.createRoute(carId))
                },
                onNavigateToCars = { },
                onNavigateToReservationsOverview = { },
                onNavigateToReservation = { },
                onNavigateToProfile = { }
            )
        }

        composable(
            route = Screen.CarDetail.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = Uri.decode(backStackEntry.arguments?.getString("carId") ?: return@composable)

            CarDetailScreen(
                carId = carId,
                onBackClick = { navController.popBackStack() },
                onReserveClick = { id ->
                    navController.navigate(Screen.Reservation.createRoute(id))
                }
            )
        }

        // ✅ Reservation -> PaymentReview met datums + kms
        composable(
            route = Screen.Reservation.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = Uri.decode(backStackEntry.arguments?.getString("carId") ?: return@composable)

            ReservationScreen(
                carId = carId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { fromDate, toDate, kms ->
                    navController.navigate(Screen.PaymentReview.createRoute(carId, fromDate, toDate, kms))
                }
            )
        }

        // ✅ PaymentReview haalt auto uit DB via carId + gebruikt fromDate/toDate/kms uit Reservation
        composable(
            route = Screen.PaymentReview.route,
            arguments = listOf(
                navArgument("carId") { type = NavType.StringType },
                navArgument("fromDate") { type = NavType.StringType },
                navArgument("toDate") { type = NavType.StringType },
                navArgument("kms") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val carId = Uri.decode(backStackEntry.arguments?.getString("carId") ?: return@composable)
            val fromDate = Uri.decode(backStackEntry.arguments?.getString("fromDate") ?: "")
            val toDate = Uri.decode(backStackEntry.arguments?.getString("toDate") ?: "")
            val kms = Uri.decode(backStackEntry.arguments?.getString("kms") ?: "")

            PaymentReviewScreen(
                carId = carId,
                fromDate = fromDate,
                toDate = toDate,
                kms = kms,
                onBackClick = { navController.popBackStack() },
                onPayClick = { navController.navigate(Screen.PaymentMethod.route) }
            )
        }

        composable(Screen.PaymentMethod.route) {
            PaymentMethodScreen(
                onBackClick = { navController.popBackStack() },
                onPaymentSelected = { /* later */ }
            )
        }
    }
}