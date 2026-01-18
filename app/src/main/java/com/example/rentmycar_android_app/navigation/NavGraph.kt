package com.example.rentmycar_android_app.navigation

import DrivingStatsScreen
import ProfileScreen
import android.net.Uri
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.core.content.edit
import com.example.rentmycar_android_app.ui.*
import com.example.rentmycar_android_app.ui.addcar.AddCarScreen
import com.example.rentmycar_android_app.ui.mycars.MyCarsScreen
import com.example.rentmycar_android_app.ui.updatecar.UpdateCarScreen
import com.example.rentmycar_android_app.viewmodels.HomeViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
     object Home : Screen("home")
     object ForgotPassword : Screen("forgot_password")

    data object CarDetail : Screen("car/{carId}") {
        fun createRoute(carId: String) = "car/${Uri.encode(carId)}"
    }

    data object Reservation : Screen("reservation/{carId}") {
        fun createRoute(carId: String) = "reservation/${Uri.encode(carId)}"
    }

    // PaymentMethod comes first - user selects payment option
    data object PaymentMethod : Screen("paymentMethod/{carId}/{fromDate}/{toDate}/{kms}") {
        fun createRoute(carId: String, fromDate: String, toDate: String, kms: String): String {
            return "paymentMethod/${Uri.encode(carId)}/${Uri.encode(fromDate)}/${Uri.encode(toDate)}/${Uri.encode(kms)}"
        }
    }

    // PaymentReview comes after - shows totals and processes payment
    data object PaymentReview : Screen("paymentReview/{carId}/{fromDate}/{toDate}/{kms}/{paymentMethod}") {
        fun createRoute(carId: String, fromDate: String, toDate: String, kms: String, paymentMethod: String): String {
            return "paymentReview/${Uri.encode(carId)}/${Uri.encode(fromDate)}/${Uri.encode(toDate)}/${Uri.encode(kms)}/${Uri.encode(paymentMethod)}"
        }
    }
    data object Map : Screen("map/{latitude}/{longitude}") {
        fun createRoute(latitude: Double, longitude: Double): String {
            return "map/${latitude}/${longitude}"
        }
    }
    object Profile : Screen("profile")
    object DrivingTracker : Screen("driving_tracker")
    object DrivingStats : Screen("driving_stats")
    object Filter : Screen("filter")
    object Reservations : Screen("reservations")
    object PaymentSuccess : Screen("payment_success")
    object MyCars : Screen("my_cars")
    object AddCar : Screen("add_car")
    data object UpdateCar : Screen("update_car/{carId}") {
        fun createRoute(carId: String) = "update_car/${Uri.encode(carId)}"
    }
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
            val homeViewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()

            HomeScreen(
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetail.createRoute(carId))
                },
                onNavigateToReservation = {
                    navController.navigate(Screen.Reservations.route)
                },
                onNavigateToCars = {
                    navController.navigate(Screen.MyCars.route)
                },
                onNavigateToReservationsOverview = {
                    navController.navigate(Screen.AddCar.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToDrivingTracker = {
                    navController.navigate(Screen.DrivingTracker.route)
                },
                onNavigateToDrivingStats = {
                    navController.navigate(Screen.DrivingStats.route)
                },
                onNavigateToFilter = {
                    navController.navigate(Screen.Filter.route)
                },
                onNavigateToMyCars = {
                    navController.navigate(Screen.MyCars.route)
                },
                viewModel = homeViewModel
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

        // Reservation -> PaymentMethod (select payment option first)
        composable(
            route = Screen.Reservation.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = Uri.decode(backStackEntry.arguments?.getString("carId") ?: return@composable)

            ReservationScreen(
                carId = carId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { fromDate, toDate, kms ->
                    navController.navigate(Screen.PaymentMethod.createRoute(carId, fromDate, toDate, kms))
                }
            )
        }

        // PaymentMethod -> PaymentReview (select option, then see totals)
        composable(
            route = Screen.PaymentMethod.route,
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

            PaymentMethodScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { paymentMethod ->
                    navController.navigate(
                        Screen.PaymentReview.createRoute(carId, fromDate, toDate, kms, paymentMethod.name)
                    )
                }
            )
        }

        // PaymentReview -> processes payment and goes to success
        composable(
            route = Screen.PaymentReview.route,
            arguments = listOf(
                navArgument("carId") { type = NavType.StringType },
                navArgument("fromDate") { type = NavType.StringType },
                navArgument("toDate") { type = NavType.StringType },
                navArgument("kms") { type = NavType.StringType },
                navArgument("paymentMethod") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val carId = Uri.decode(backStackEntry.arguments?.getString("carId") ?: return@composable)
            val fromDate = Uri.decode(backStackEntry.arguments?.getString("fromDate") ?: "")
            val toDate = Uri.decode(backStackEntry.arguments?.getString("toDate") ?: "")
            val kms = Uri.decode(backStackEntry.arguments?.getString("kms") ?: "")
            val paymentMethod = Uri.decode(backStackEntry.arguments?.getString("paymentMethod") ?: "")

            PaymentReviewScreen(
                carId = carId,
                fromDate = fromDate,
                toDate = toDate,
                kms = kms,
                paymentMethod = paymentMethod,
                onBackClick = { navController.popBackStack() },
                onPaymentSuccess = {
                    navController.navigate(Screen.PaymentSuccess.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.Map.route,
            arguments = listOf(
                navArgument("latitude") { type = NavType.StringType },
                navArgument("longitude") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0

            MapScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                carLatitude = latitude,
                carLongitude = longitude
            )
        }

        composable(Screen.PaymentSuccess.route) {
            PaymentSuccessScreen(
                onBackClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onViewReservationsClick = {
                    navController.navigate(Screen.Reservations.route) {
                        popUpTo(Screen.Home.route)
                    }
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

        composable(Screen.Filter.route) {
            val homeEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeViewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(homeEntry)

            FilterScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onApplyFilters = { filterState ->
                    homeViewModel.applyFilter(filterState)
                },
                initialFilterState = homeViewModel.getCurrentFilter()
            )
        }

        composable(Screen.Reservations.route) {
            ReservationsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToLocation = { lat: Double, lon: Double ->
                    navController.navigate(Screen.Map.createRoute(lat, lon))
                }
            )
        }

        composable(Screen.MyCars.route) {
            MyCarsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCarClick = { carId ->
                    navController.navigate(Screen.UpdateCar.createRoute(carId))
                },
                onAddCarClick = {
                    navController.navigate(Screen.AddCar.route)
                }
            )
        }

        composable(Screen.AddCar.route) {
            AddCarScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCarAdded = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.UpdateCar.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = Uri.decode(backStackEntry.arguments?.getString("carId") ?: return@composable)

            UpdateCarScreen(
                carId = carId,
                onBackClick = {
                    navController.popBackStack()
                },
                onCarUpdated = {
                    navController.popBackStack()
                },
                onCarDeleted = {
                    navController.navigate(Screen.MyCars.route) {
                        popUpTo(Screen.MyCars.route) { inclusive = true }
                    }
                }
            )
        }
    }
}