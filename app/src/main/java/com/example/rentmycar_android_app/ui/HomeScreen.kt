package com.example.rentmycar_android_app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentmycar_android_app.car.CarDto
import com.example.rentmycar_android_app.viewmodels.HomeViewModel
import com.example.rentmycar_android_app.viewmodels.HomeViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onCarClick: (String) -> Unit,
//    onNavigateToCars: () -> Unit,
//    onNavigateToReservationsOverview: () -> Unit,
    onNavigateToReservation: () -> Unit,
    onNavigateToCars: () -> Unit = {},
    onNavigateToReservationsOverview: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDrivingTracker: () -> Unit = {},
    onNavigateToDrivingStats: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val username = sharedPrefs.getString("username", "Onbekend") ?: "Onbekend"

    val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(context))
    val uiState by vm.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            HomeBottomBar(
                onHomeClick = {},
                onExploreClick = onNavigateToCars,
                onFavoritesClick = onNavigateToReservationsOverview,
                onKeysClick = onNavigateToReservation,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {

            // Header met titel en Start Rit knop
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Welkom terug, $username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }

                Card(
                    modifier = Modifier
                        .clickable { onNavigateToDrivingTracker() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Row(
                        modifier = Modifier. padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Start Rit",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            LocationSearchCard()

            Spacer(modifier = Modifier. height(16.dp))

            Text(
                text = "Auto's",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Fout bij laden auto's:\n${uiState.error}",
                            color = Color.Red
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 90.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.cars) { car ->
                            CarCard(
                                car = car,
                                onClick = {
                                    if (car.id.isNotBlank()) {
                                        onCarClick(car.id)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Auto id ontbreekt (backend stuurt _id)",
                                                withDismissAction = true
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationSearchCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF8F8F99))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Location",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Amsterdam, Nederland",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SearchField(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                FilterButton()
            }
        }
    }
}

@Composable
private fun SearchField(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp)),
        placeholder = { Text("Vul locatie in") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF0E9E9),
            focusedContainerColor = Color(0xFFF0E9E9),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
        )
    )
}

@Composable
private fun FilterButton() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF0E9E9)),
        contentAlignment = Alignment.Center
    ) {
        Text("≡", fontSize = 18.sp)
    }
}

@Composable
private fun CarCard(
    car: CarDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7E7))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDCD3D3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = Color.DarkGray
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${car.brand.orEmpty()} ${car.model.orEmpty()}",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "€${(car.pricePerTimeSlot ?: 0.0).toInt()}/dag",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            if (car.costPerKm != null) {
                Text(
                    text = "€${car.costPerKm} p/km",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onKeysClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(containerColor = Color(0xFFF3F3F3)) {
        NavigationBarItem(
            selected = true,
            onClick = onHomeClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onExploreClick,
            icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
            label = { Text("Explore") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onFavoritesClick,
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite") },
            label = { Text("Favorite") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onKeysClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Key") }, // placeholder
            label = { Text("Key") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}