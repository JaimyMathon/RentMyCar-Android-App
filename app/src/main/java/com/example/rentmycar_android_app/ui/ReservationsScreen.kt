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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.network.ReservationService
import kotlinx.coroutines.launch
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
                val service = ApiClientWithToken(context).instance.create(ReservationService::class.java)
                reservations = service.getReservations()
                android.util.Log.d("ReservationsScreen", "Successfully loaded ${reservations.size} reservations")
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
                                                val service = ApiClientWithToken(context)
                                                    .instance.create(ReservationService::class.java)
                                                service.cancelReservation(id)
                                                // Refresh reservations
                                                reservations = service.getReservations()
                                            } catch (e: Exception) {
                                                error = e.message ?: "Fout bij annuleren"
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
    val car = reservation.car
    
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
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Auto",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status Badge
            Surface(
                color = when (reservation.status?.lowercase()) {
                    "confirmed" -> Color(0xFF4CAF50)
                    "pending" -> Color(0xFFFFC107)
                    "cancelled" -> Color(0xFFF44336)
                    "completed" -> Color(0xFF2196F3)
                    else -> Color.Gray
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = reservation.status?.uppercase() ?: "UNKNOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Car Category
            Text(
                text = car?.category ?: "N/A",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Car Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${car?.brand ?: "Unknown"} ${car?.model ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (car?.licensePlate != null) {
                        Text(
                            text = "📋 ${car.licensePlate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = "€${car?.pricePerTimeSlot?.toInt() ?: 0}/dag",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Reservation Details Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
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
                                color = Color.Gray
                            )
                            Text(
                                text = formatDateTime(reservation.endTime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (reservation.estimatedDistance != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Geschatte afstand",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "${reservation.estimatedDistance} km",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (car?.costPerKm != null && reservation.estimatedDistance != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kosten per km",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "€${String.format("%.2f", car.costPerKm)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location and Navigate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Locatie",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lat: ${String.format("%.4f", car?.latitude ?: 0.0)}, Lon: ${String.format("%.4f", car?.longitude ?: 0.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                TextButton(
                    onClick = {
                        car?.let {
                            onNavigateClick(it.latitude, it.longitude)
                        }
                    }
                ) {
                    Text("Navigeer")
                }
            }

            // Cancel Button
            if (showCancelButton) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onCancelClick(reservation.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Annuleren", color = Color.White)
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
