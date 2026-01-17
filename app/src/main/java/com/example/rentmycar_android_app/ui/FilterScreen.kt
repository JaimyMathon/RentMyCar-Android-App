package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FilterState(
    val selectedTypes: Set<String> = emptySet(),
    val maxPricePerKm: Float = 0.70f,
    val maxPricePerDay: Float = 300f,
    val selectedBrands: Set<String> = emptySet()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onBackClick: () -> Unit,
    onApplyFilters: (FilterState) -> Unit,
    initialFilterState: FilterState = FilterState(),
    viewModel: FilterViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val availableBrands by viewModel.availableBrands.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    LaunchedEffect(initialFilterState) {
        viewModel.initializeFilter(initialFilterState)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Terug"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Types Section
            Text(
                text = "Types",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FilterChipGroup(
                options = listOf("All", "ICE", "BEV", "FCEV"),
                selectedOptions = filterState.selectedTypes,
                onSelectionChange = { viewModel.updateSelectedTypes(it) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Price per Km Section
            Text(
                text = "Prijs per Km",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PriceSlider(
                minValue = 0.10f,
                maxValue = 0.70f,
                currentValue = filterState.maxPricePerKm,
                onValueChange = { viewModel.updateMaxPricePerKm(it) },
                valueFormatter = { "€%.2f".format(it) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Price per Day Section
            Text(
                text = "Prijs per dag",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PriceSlider(
                minValue = 50f,
                maxValue = 300f,
                currentValue = filterState.maxPricePerDay,
                onValueChange = { viewModel.updateMaxPricePerDay(it) },
                valueFormatter = { "€${it.toInt()}" },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Brands Section
            Text(
                text = "Merken",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FilterChipGroup(
                options = listOf("All") + availableBrands,
                selectedOptions = filterState.selectedBrands,
                onSelectionChange = { viewModel.updateSelectedBrands(it) },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reset Button
                Button(
                    onClick = { viewModel.resetFilters() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E9E9E)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        "Reset",
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Apply Button
                Button(
                    onClick = {
                        onApplyFilters(viewModel.getCurrentFilterState())
                        onBackClick()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        "Toepassen",
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOptions.contains(option) ||
                            (option == "All" && selectedOptions.isEmpty())

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color(0xFF757575)
                        else Color(0xFFE0E0E0)
                    )
                    .clickable {
                        if (option == "All") {
                            onSelectionChange(emptySet())
                        } else {
                            val newSelection = if (selectedOptions.contains(option)) {
                                selectedOptions - option
                            } else {
                                selectedOptions + option
                            }
                            onSelectionChange(newSelection)
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceSlider(
    minValue: Float,
    maxValue: Float,
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    valueFormatter: (Float) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Current value display
        Text(
            text = valueFormatter(currentValue),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Slider
        Slider(
            value = currentValue,
            onValueChange = onValueChange,
            valueRange = minValue..maxValue,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF757575),
                activeTrackColor = Color(0xFF757575),
                inactiveTrackColor = Color(0xFFE0E0E0)
            )
        )

        // Min/Max labels
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