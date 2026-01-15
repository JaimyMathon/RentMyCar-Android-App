package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.rentmycar_android_app.R
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CarService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: String,
    onBackClick: () -> Unit,
    onReserveClick: (String) -> Unit
) {
    val context = LocalContext.current
    val carService = remember { ApiClientWithToken(context).instance.create(CarService::class.java) }

    var loading by remember { mutableStateOf(true) }
    var car by remember { mutableStateOf<CarDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val errorFetchingCarText = stringResource(R.string.error_fetching_car)

    LaunchedEffect(carId) {
        loading = true
        error = null
        try {
            car = carService.getCarById(carId)
        } catch (e: Exception) {
            error = e.message ?: errorFetchingCarText
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.car_details_title)) },
                navigationIcon = { TextButton(onClick = onBackClick) { Text(stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            when {
                loading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                car != null -> {
                    val c = car!!

                    Text("${c.brand.orEmpty()} ${c.model.orEmpty()}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("€ ${(c.pricePerTimeSlot ?: 0.0).toInt()}${stringResource(R.string.per_day)}")
                    Text("€ ${(c.costPerKm ?: 0.0)} ${stringResource(R.string.per_km)}")

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onReserveClick(c.safeId) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.reserve)) }
                }
            }
        }
    }
}