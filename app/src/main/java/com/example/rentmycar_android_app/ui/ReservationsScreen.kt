package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.CarService
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.network.ReservationService
import com.example.rentmycar_android_app.network.SimpleResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    onBackClick: () -> Unit,
    onNavigateToLocation: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var reservations by remember { mutableStateOf<List<ReservationDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val tabs = listOf("Komend", "Afgehandeld", "cancelled")
    
    // Load reservations
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("jwt_token", null)

            android.util.Log.d("ReservationsScreen", "Token check: ${if (token.isNullOrEmpty()) "EMPTY/NULL" else "Present"}")

            if (token.isNullOrEmpty()) {
                error = "Geen token gevonden. Log opnieuw in."
                android.util.Log.e("ReservationsScreen", "No token in SharedPreferences")
            } else {
                android.util.Log.d("ReservationsScreen", "Attempting to fetch reservations...")
                val reservationService = ApiClientWithToken(context).instance.create(ReservationService::class.java)
                val loadedReservations = reservationService.getReservations()
                android.util.Log.d("ReservationsScreen", "Successfully loaded ${loadedReservations.size} reservations")

                // Fetch car details for reservations that don't have car data
                val carService = ApiClientWithToken(context).instance.create(CarService::class.java)
                reservations = loadedReservations.map { reservation ->
                    if (reservation.car == null && reservation.carId != null) {
                        try {
                            val car = carService.getCarById(reservation.carId)
                            android.util.Log.d("ReservationsScreen", "Fetched car details for carId: ${reservation.carId}")
                            reservation.copy(car = car)
                        } catch (e: Exception) {
                            android.util.Log.e("ReservationsScreen", "Failed to fetch car for carId: ${reservation.carId}", e)
                            reservation
                        }
                    } else {
                        reservation
                    }
                }
                android.util.Log.d("ReservationsScreen", "Finished fetching car details")
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("ReservationsScreen", "HTTP ${e.code()}: ${e.message()}, Body: $errorBody")
            error = "HTTP ${e.code()}: ${e.message()}"
        } catch (e: Exception) {
            android.util.Log.e("ReservationsScreen", "Error loading reservations", e)
            error = "Fout: ${e.message}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }
    
    // Filter reservations by status
    val filteredReservations = remember(reservations, selectedTab) {
        when (selectedTab) {
            0 -> reservations.filter { it.status?.lowercase() == "pending" || it.status?.lowercase() == "confirmed" }
            1 -> reservations.filter { it.status?.lowercase() == "completed" }
            2 -> reservations.filter { it.status?.lowercase() == "cancelled" }
            else -> emptyList()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mijn reserveringen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Content
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                    filteredReservations.isEmpty() -> Text("Geen reserveringen gevonden")
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredReservations) { reservation ->
                                ReservationCard(
                                    reservation = reservation,
                                    showCancelButton = selectedTab == 0,
                                    onCancelClick = { id ->
                                        scope.launch {
                                            try {
                                                val reservationService = ApiClientWithToken(context)
                                                    .instance.create(ReservationService::class.java)
                                                reservationService.cancelReservation(id)
                                                // Refresh reservations and fetch car details
                                                val loadedReservations = reservationService.getReservations()
                                                val carService = ApiClientWithToken(context).instance.create(CarService::class.java)
                                                reservations = loadedReservations.map { reservation ->
                                                    if (reservation.car == null && reservation.carId != null) {
                                                        try {
                                                            val car = carService.getCarById(reservation.carId)
                                                            reservation.copy(car = car)
                                                        } catch (e: Exception) {
                                                            reservation
                                                        }
                                                    } else {
                                                        reservation
                                                    }
                                                }
                                            } catch (e: retrofit2.HttpException) {
                                                // Parse the error response from the API
                                                val errorBody = e.response()?.errorBody()?.string()
                                                error = try {
                                                    val errorResponse = Gson().fromJson(errorBody, SimpleResponse::class.java)
                                                    errorResponse.message
                                                } catch (ex: Exception) {
                                                    "Fout bij annuleren: ${e.code()}"
                                                }
                                                android.util.Log.e("ReservationsScreen", "Cancel failed: $errorBody")
                                            } catch (e: Exception) {
                                                error = e.message ?: "Fout bij annuleren"
                                                android.util.Log.e("ReservationsScreen", "Cancel error", e)
                                            }
                                        }
                                    },
                                    onNavigateClick = { lat, lon ->
                                        onNavigateToLocation(lat, lon)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: ReservationDto,
    showCancelButton: Boolean,
    onCancelClick: (String) -> Unit,
    onNavigateClick: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val car = reservation.car

    // State to hold the photo URL
    var photoUrl by remember { mutableStateOf<String?>(null) }

    // Fetch car photo
    LaunchedEffect(car?.id) {
        if (car?.id != null) {
            try {
                val carService = ApiClientWithToken(context).instance.create(CarService::class.java)
                val photos = carService.getCarPhotos(car.id)
                if (photos.isNotEmpty()) {
                    // Construct full URL: base URL + photo path
                    photoUrl = "http://10.0.2.2:8080${photos[0].url}"
                    android.util.Log.d("ReservationCard", "Loaded photo URL: $photoUrl")
                }
            } catch (e: Exception) {
                android.util.Log.e("ReservationCard", "Failed to load car photo: ${e.message}")
            }
        }
    }

    // Check if reservation has already started
    val hasStarted = try {
        reservation.startTime?.let { startTime ->
            val startInstant = ZonedDateTime.parse(startTime).toInstant()
            val now = java.time.Instant.now()
            startInstant <= now
        } ?: false
    } catch (e: Exception) {
        false
    }

    // Only show cancel button if allowed and reservation hasn't started yet
    val canCancel = showCancelButton && !hasStarted

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Car Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF2196F3), RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Auto foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder icon
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Auto",
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Car Category
            Text(
                text = car?.category ?: "Sedan",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Car Name and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${car?.brand ?: "Unknown"} ${car?.model ?: ""}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "€${car?.pricePerTimeSlot?.toInt() ?: 0}/dag",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF757575)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fuel Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Brandstof",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ICE",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reservation Dates
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = formatDateTime(reservation.startTime),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Eind",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = formatDateTime(reservation.endTime),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location and Navigate Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "auto Locatie",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )

                Button(
                    onClick = {
                        if (car != null) {
                            onNavigateClick(car.latitude, car.longitude)
                        } else if (reservation.carId != null) {
                            scope.launch {
                                try {
                                    val carService = ApiClientWithToken(context)
                                        .instance.create(CarService::class.java)
                                    val fetchedCar = carService.getCarById(reservation.carId)
                                    onNavigateClick(fetchedCar.latitude, fetchedCar.longitude)
                                } catch (e: Exception) {
                                    android.util.Log.e("ReservationCard", "Error fetching car details: ${e.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E9E9E)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Navigate",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Cancel Button
            if (canCancel) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onCancelClick(reservation.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E9E9E)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        "Annuleren",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// Helper function to format ISO date string to readable format
private fun formatDateTime(isoString: String?): String {
    if (isoString.isNullOrEmpty()) return "N/A"

    return try {
        val zonedDateTime = ZonedDateTime.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        isoString.substringBefore("T")
    }
}
