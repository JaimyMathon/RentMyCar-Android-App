package com.example.rentmycar_android_app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.rentmycar_android_app.network.CarDto

@Composable
fun HomeScreen(
    onNavigateToReservation: () -> Unit,
    onNavigateToCars: () -> Unit = {},
    onNavigateToReservationsOverview: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val username = sharedPrefs.getString("username", "Onbekend")

    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    if (showFilterDialog) {
        FilterDialog(
            filterState = uiState.filterState,
            availableBrands = uiState.availableBrands,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilter ->
                viewModel.updateFilters(newFilter)
            },
            onReset = {
                viewModel.resetFilters()
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                HomeBottomBar(
                    onHomeClick = { /* al op Home */ },
                    onExploreClick = onNavigateToCars,                 // Explore -> auto's bekijken
                    onFavoritesClick = onNavigateToReservationsOverview,
                    onKeysClick = onNavigateToReservation,            // Key-tab -> reserveringen
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

            // Titel bovenaan
            Text(
                text = "Home",
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Welkom-tekst
            Text(
                text = "Welkom terug, $username",
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            // Locatie + zoekveld (grijze kaart)
            LocationSearchCard(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { query ->
                    viewModel.updateSearchQuery(query)
                },
                onFilterClick = { showFilterDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Auto’s",
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
                        items(uiState.filteredCars) { car ->
                            CarCard(car = car)
                        }
                    }
                }
            }
        }
        }
    }
}



// --------- BOVENSTE KAART: LOCATIE + ZOEK ---------

@Composable
private fun LocationSearchCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
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
                text = "Location",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterButton(onClick = onFilterClick)
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp)),
        placeholder = { Text("Zoek op merk, model of stad") },
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
private fun FilterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF0E9E9))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Simpele placeholder voor filter-icoon
        Text("≡", fontSize = 18.sp)
    }
}

// --------- AUTO KAART ---------

@Composable
private fun CarCard(car: CarDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEDE7E7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            // Boven: rating + favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (car.rating != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", car.rating),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Afbeelding placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDCD3D3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home, // placeholder ipv echte afbeelding
                    contentDescription = null,
                    tint = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!car.bodyType.isNullOrBlank()) {
                    Text(
                        text = car.bodyType,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.Gray
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                Text(
                    text = "€${car.pricePerDay.toInt()}/dag",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${car.brand} ${car.model}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = car.city ?: "",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = car.category ?: "",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                if (car.pricePerKm != null) {
                    Text(
                        text = String.format("€%.2f p/km", car.pricePerKm),
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

// --------- FILTER DIALOG ---------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    filterState: FilterState,
    availableBrands: List<String>,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit,
    onReset: () -> Unit
) {
    var currentFilter by remember(filterState) { mutableStateOf(filterState) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Filter", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Types",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FilterChipGroup(
                        options = listOf("All", "ICE", "BEV", "FCEV"),
                        selectedOptions = currentFilter.selectedTypes,
                        onSelectionChange = { newSelection ->
                            currentFilter = if ("All" in newSelection && "All" !in currentFilter.selectedTypes) {
                                currentFilter.copy(selectedTypes = setOf("All"))
                            } else if (newSelection.size > 1 && "All" in newSelection) {
                                currentFilter.copy(selectedTypes = newSelection - "All")
                            } else if (newSelection.isEmpty()) {
                                currentFilter.copy(selectedTypes = setOf("All"))
                            } else {
                                currentFilter.copy(selectedTypes = newSelection)
                            }
                        }
                    )
                }

                item {
                    Text(
                        text = "Prijs per Km",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PriceRangeSlider(
                        range = currentFilter.pricePerKmRange,
                        onRangeChange = { newRange ->
                            currentFilter = currentFilter.copy(pricePerKmRange = newRange)
                        },
                        minValue = 0.0f,
                        maxValue = 0.5f,
                        valueFormatter = { "€%.2f".format(it) },
                        steps = 100  // 100 steps between 0 and 10 (0.10 increments)
                    )
                }

                item {
                    Text(
                        text = "Prijs per dag",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PriceRangeSlider(
                        range = currentFilter.pricePerDayRange,
                        onRangeChange = { newRange ->
                            currentFilter = currentFilter.copy(pricePerDayRange = newRange)
                        },
                        minValue = 0f,
                        maxValue = 500f,
                        valueFormatter = { "€%.0f".format(it) },
                        steps = 50  // 100 steps between 0 and 1000 (10 increments)
                    )
                }

                item {
                    Text(
                        text = "Merken",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FilterChipGroup(
                        options = listOf("All") + availableBrands,
                        selectedOptions = currentFilter.selectedBrands,
                        onSelectionChange = { newSelection ->
                            currentFilter = if ("All" in newSelection && "All" !in currentFilter.selectedBrands) {
                                currentFilter.copy(selectedBrands = setOf("All"))
                            } else if (newSelection.size > 1 && "All" in newSelection) {
                                currentFilter.copy(selectedBrands = newSelection - "All")
                            } else if (newSelection.isEmpty()) {
                                currentFilter.copy(selectedBrands = setOf("All"))
                            } else {
                                currentFilter.copy(selectedBrands = newSelection)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onReset()
                        currentFilter = FilterState()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8F8F99)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Reset", color = Color.White)
                }

                Button(
                    onClick = {
                        onApply(currentFilter)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8F8F99)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Toepassen", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FilterChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option in selectedOptions
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color(0xFF8F8F99) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else Color(0xFFCCCCCC),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        val newSelection = if (isSelected) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChange(newSelection)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else Color(0xFF666666),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun PriceRangeSlider(
    range: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    minValue: Float,
    maxValue: Float,
    valueFormatter: (Float) -> String,
    steps: Int = 0
) {
    Column {
        // Show current selected value
        Text(
            text = valueFormatter(range.endInclusive),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Slider(
            value = range.endInclusive,
            onValueChange = { newEnd ->
                // Manually snap to steps to hide step dots
                val stepSize = (maxValue - minValue) / (steps + 1)
                val snappedValue = minValue + (kotlin.math.round((newEnd - minValue) / stepSize) * stepSize)
                onRangeChange(range.start..snappedValue)
            },
            valueRange = minValue..maxValue,
            steps = 0,  // No visual steps, we handle snapping manually
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF8F8F99),
                activeTrackColor = Color(0xFF8F8F99),
                inactiveTrackColor = Color(0xFFE0E0E0)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = valueFormatter(minValue),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = valueFormatter(maxValue),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// --------- BOTTOM NAV ---------

@Composable
private fun HomeBottomBar(
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onKeysClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFF3F3F3)
    ) {
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