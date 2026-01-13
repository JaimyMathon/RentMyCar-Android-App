package com.example.rentmycar_android_app.ui.car

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rentmycar_android_app.core.network.ApiClientWithToken
import com.example.rentmycar_android_app.car.CarDto
import com.example.rentmycar_android_app.car.CarService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: String,
    onBackClick: () -> Unit,
    onReserveClick: (String) -> Unit
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var car by remember { mutableStateOf<CarDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(carId) {
        isLoading = true
        error = null
        car = null

        try {
            val service = ApiClientWithToken(context).instance.create(CarService::class.java)
            car = service.getCarById(carId)
        } catch (e: Exception) {
            error = e.message ?: "Fout bij ophalen auto"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Text("<") }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!)
                car != null -> {
                    val c = car!!

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${c.brand.orEmpty()} ${c.model.orEmpty()}",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.height(12.dp))

                        Text("Prijs per dag: €${(c.pricePerTimeSlot ?: 0.0).toInt()}")
                        Text("Prijs per km: €${c.costPerKm ?: 0.0}")
                        Text("Categorie: ${c.category.orEmpty()}")
                        Text("Status: ${c.status.orEmpty()}")
                        Text("TCO: ${c.tco ?: 0.0}")
                        Text("Toegevoegd door: ${c.addedBy.orEmpty()}")

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { onReserveClick(carId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reserveren")
                        }
                    }
                }
            }
        }
    }
}