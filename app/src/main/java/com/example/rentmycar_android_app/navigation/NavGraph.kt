package com.example.rentmycar_android_app.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    data object ForgotPassword : Screen("forgot_password")
    data object AddCar : Screen("add_car")

    data object CarDetail : Screen("car/{carId}") {
        fun createRoute(carId: String) = "car/${Uri.encode(carId)}"
    }

    data object Reservation : Screen("reservation/{carId}") {
        fun createRoute(carId: String) = "reservation/${Uri.encode(carId)}"
    }

    data object PaymentReview : Screen("paymentReview/{carId}/{fromDate}/{toDate}/{kms}") {
        fun createRoute(carId: String, fromDate: String, toDate: String, kms: String): String {
            return "paymentReview/${Uri.encode(carId)}/${Uri.encode(fromDate)}/${Uri.encode(toDate)}/${Uri.encode(kms)}"
        }
    }

    data object PaymentMethod : Screen("paymentMethod")
    data object Map : Screen("map")
    data object Profile : Screen("profile")
    data object DrivingTracker : Screen("driving_tracker")
    data object DrivingStats : Screen("driving_stats")
    data object Filter : Screen("filter")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // LOGIN
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        // REGISTER
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

        // FORGOT PASSWORD
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }

        // HOME
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = HomeViewModelFactory(context)
            )

            // ✅ refresh trigger: AddCar -> Home
            val homeEntry = remember { navController.getBackStackEntry(Screen.Home.route) }
            val refreshFlow = remember(homeEntry) {
                homeEntry.savedStateHandle.getStateFlow("cars_refresh", false)
            }
            val refresh by refreshFlow.collectAsStateWithLifecycle()

            LaunchedEffect(refresh) {
                if (refresh) {
                    homeViewModel.loadCars()
                    homeEntry.savedStateHandle["cars_refresh"] = false
                }
            }

            HomeScreen(
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetail.createRoute(carId))
                },
                onNavigateToAddCar = { navController.navigate(Screen.AddCar.route) },
                onNavigateToReservation = { /* leeg */ },
                onNavigateToCars = { /* later */ },
                onNavigateToReservationsOverview = { /* later */ },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToDrivingTracker = { navController.navigate(Screen.DrivingTracker.route) },
                onNavigateToDrivingStats = { navController.navigate(Screen.DrivingStats.route) },
                onNavigateToFilter = { navController.navigate(Screen.Filter.route) },
                viewModel = homeViewModel
            )
        }

        // ADD CAR
        composable(Screen.AddCar.route) {
            AddCarScreen(
                onBack = { navController.popBackStack() },
                onCarAdded = {
                    navController.getBackStackEntry(Screen.Home.route)
                        .savedStateHandle["cars_refresh"] = true
                    navController.popBackStack()
                }
            )
        }

        // CAR DETAIL
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

        // RESERVATION
        composable(
            route = Screen.Reservation.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = Uri.decode(backStackEntry.arguments?.getString("carId") ?: return@composable)

            ReservationScreen(
                carId = carId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { fromDate, toDate, kms ->
                    navController.navigate(
                        Screen.PaymentReview.createRoute(carId, fromDate, toDate, kms)
                    )
                }
            )
        }

        // PAYMENT REVIEW
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

        // MAP
        composable(Screen.Map.route) {
            MapScreen(onNavigateBack = { navController.popBackStack() })
        }

        // PAYMENT METHOD
        composable(Screen.PaymentMethod.route) {
            PaymentMethodScreen(
                onBackClick = { navController.popBackStack() },
                onPaymentSelected = { }
            )
        }

        // PROFILE
        composable(Screen.Profile.route) {
            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("jwt_token", "")

            ProfileScreen(
                token = token,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDrivingStats = { navController.navigate(Screen.DrivingStats.route) },
                onLogout = {
                    sharedPrefs.edit { clear() }
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // DRIVING TRACKER
        composable(Screen.DrivingTracker.route) {
            DrivingTrackerScreen(onNavigateBack = { navController.popBackStack() })
        }

        // DRIVING STATS
        composable(Screen.DrivingStats.route) {
            DrivingStatsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // FILTER (compile-proof: geen applyFilter/getCurrentFilter meer)
        composable(Screen.Filter.route) {
            FilterScreen(
                onBackClick = { navController.popBackStack() },

                // ✅ Als jij filters nog niet gebouwd hebt: doe voorlopig niks en ga terug
                onApplyFilters = { _ ->
                    // eventueel: navController.popBackStack()
                },

                // ✅ Als FilterState bij jou bestaat, kun je hier default meegeven.
                // Anders: zet je FilterScreen zo dat initialFilterState optional is.
                initialFilterState = FilterState()
            )
        }
    }
}