package com.example.rentmycar_android_app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentmycar_android_app.R
import com.example.rentmycar_android_app.network.CarDto
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onCarClick: (String) -> Unit,
    onNavigateToAddCar: () -> Unit = {},
    onNavigateToReservation: () -> Unit = {},
    onNavigateToCars: () -> Unit = {},
    onNavigateToReservationsOverview: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDrivingTracker: () -> Unit = {},
    onNavigateToDrivingStats: () -> Unit = {},
    onNavigateToFilter: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val unknownText = stringResource(R.string.unknown)
    val username = sharedPrefs.getString("username", unknownText) ?: unknownText

    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val carIdMissingText = stringResource(R.string.car_id_missing)

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.welcome_back, username),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onNavigateToAddCar,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(stringResource(R.string.add_car)) }

                    Card(
                        modifier = Modifier.clickable { onNavigateToDrivingTracker() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                stringResource(R.string.start_trip),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            LocationSearchCard(
                onFilterClick = onNavigateToFilter,
                onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.cars_title),
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
                    ) { CircularProgressIndicator() }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.error_loading_cars, uiState.error ?: ""),
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
                                    val id = car.safeId
                                    if (id.isNotBlank()) {
                                        onCarClick(id)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = carIdMissingText,
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
private fun LocationSearchCard(
    onFilterClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
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
                text = stringResource(R.string.location),
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
                SearchField(
                    modifier = Modifier.weight(1f),
                    onQueryChange = onSearchQueryChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterButton(onClick = onFilterClick)
            }
        }
    }
}

@Composable
private fun SearchField(
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    OutlinedTextField(
        value = query,
        onValueChange = {
            query = it
            onQueryChange(it)
        },
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp)),
        placeholder = { Text(stringResource(R.string.search_cars)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
private fun FilterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF0E9E9))
            .clickable { onClick() },
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
    val context = LocalContext.current
    val perDayText = stringResource(R.string.per_day)
    val perKmText = stringResource(R.string.per_km)

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
                val imageUrl = car.carImageUrl
                if (!imageUrl.isNullOrBlank()) {
                    // Bouw volledige URL als het een relatief pad is
                    val fullUrl = if (imageUrl.startsWith("http")) {
                        imageUrl
                    } else {
                        "http://10.0.2.2:8080/$imageUrl"
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fullUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${car.brand} ${car.model}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback icoon als geen afbeelding
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(48.dp)
                    )
                }
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
                text = "€${(car.pricePerTimeSlot ?: 0.0).toInt()}$perDayText",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "€${car.costPerKm ?: 0.0} $perKmText",
                fontSize = 13.sp,
                color = Color.DarkGray
            )
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
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
            label = { Text(stringResource(R.string.nav_home)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onExploreClick,
            icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.nav_explore)) },
            label = { Text(stringResource(R.string.nav_explore)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onFavoritesClick,
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = stringResource(R.string.nav_favorite)) },
            label = { Text(stringResource(R.string.nav_favorite)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onKeysClick,
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_key)) },
            label = { Text(stringResource(R.string.nav_key)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.nav_profile)) },
            label = { Text(stringResource(R.string.nav_profile)) }
        )
    }
}
