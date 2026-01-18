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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentmycar_android_app.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.viewmodels.ReservationsViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    onBackClick: () -> Unit,
    onNavigateToLocation: (Double, Double) -> Unit = { _, _ -> },
    viewModel: ReservationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        stringResource(R.string.upcoming),
        stringResource(R.string.completed),
        stringResource(R.string.cancelled)
    )

    // Filter reservations by status
    val filteredReservations = remember(uiState.reservations, selectedTab) {
        when (selectedTab) {
            0 -> uiState.reservations.filter { it.status?.lowercase() == "pending" || it.status?.lowercase() == "confirmed" }
            1 -> uiState.reservations.filter { it.status?.lowercase() == "completed" }
            2 -> uiState.reservations.filter { it.status?.lowercase() == "cancelled" }
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_reservations)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                    uiState.isLoading -> CircularProgressIndicator()
                    uiState.error != null -> Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    filteredReservations.isEmpty() -> Text(stringResource(R.string.no_reservations_found))
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
                                        viewModel.cancelReservation(id)
                                    },
                                    onNavigateClick = { lat, lon ->
                                        onNavigateToLocation(lat, lon)
                                    },
                                    viewModel = viewModel
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
    onNavigateClick: (Double, Double) -> Unit,
    viewModel: ReservationsViewModel
) {
    val context = LocalContext.current
    val car = reservation.car

    // State to hold the photo URL
    var photoUrl by remember { mutableStateOf<String?>(null) }

    // Fetch car photo using ViewModel
    LaunchedEffect(car?.id) {
        if (car?.id != null) {
            photoUrl = viewModel.getCarPhotoUrl(car.id)
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
                        contentDescription = stringResource(R.string.car_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder icon
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(R.string.car_photo),
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(4.dp))

            // Car Name and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${car?.brand ?: stringResource(R.string.unknown)} ${car?.model ?: ""}",
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = car?.category ?: "",
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
                            text = stringResource(R.string.start),
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
                            text = stringResource(R.string.end),
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
                    text = stringResource(R.string.car_location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )

                Button(
                    onClick = {
                        if (car != null) {
                            onNavigateClick(car.latitude, car.longitude)
                        }
                    },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B6B6B)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        stringResource(R.string.navigate),
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
                        containerColor = Color(0xFF6B6B6B)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        stringResource(R.string.cancel_reservation),
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