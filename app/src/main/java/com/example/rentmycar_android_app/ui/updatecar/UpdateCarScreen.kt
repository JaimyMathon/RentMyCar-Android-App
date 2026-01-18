package com.example.rentmycar_android_app.ui.updatecar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentmycar_android_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCarScreen(
    carId: String,
    onBackClick: () -> Unit,
    onCarUpdated: () -> Unit,
    onCarDeleted: () -> Unit,
    viewModel: UpdateCarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Form state - initialize from car data when loaded
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ICE") }
    var pricePerTimeSlot by remember { mutableStateOf("") }
    var costPerKm by remember { mutableStateOf("") }
    var fuelCost by remember { mutableStateOf("") }
    var maintenance by remember { mutableStateOf("") }
    var insurance by remember { mutableStateOf("") }
    var depreciation by remember { mutableStateOf("") }

    // Address state
    var streetName by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Nederland") }

    // Initialize form when car is loaded
    LaunchedEffect(uiState.car) {
        uiState.car?.let { car ->
            brand = car.brand ?: ""
            model = car.model ?: ""
            licensePlate = car.licensePlate ?: ""
            selectedCategory = car.category ?: "ICE"
            pricePerTimeSlot = (car.pricePerTimeSlot ?: 0.0).toString()
            costPerKm = (car.costPerKm ?: 0.0).toString()
            fuelCost = (car.fuelCost ?: 0.0).toString()
            maintenance = (car.maintenance ?: 0.0).toString()
            insurance = (car.insurance ?: 0.0).toString()
            depreciation = (car.depreciation ?: 0.0).toString()
        }
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            onCarUpdated()
        }
    }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onCarDeleted()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_car)) },
            text = { Text(stringResource(R.string.delete_car_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCar()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_car), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !uiState.isDeleting
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.car == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Fout: ${uiState.error}", color = Color.Red)
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Car Details Section
                    Text(stringResource(R.string.car_details_section), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text(stringResource(R.string.brand)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text(stringResource(R.string.model)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it.uppercase() },
                        label = { Text(stringResource(R.string.license_plate)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Selection
                    Text(stringResource(R.string.category), fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ICE", "BEV", "FCEV").forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                leadingIcon = if (selectedCategory == category) {
                                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pricing Section
                    Text(stringResource(R.string.prices), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pricePerTimeSlot,
                            onValueChange = { pricePerTimeSlot = it },
                            label = { Text(stringResource(R.string.price_day)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("€") }
                        )

                        OutlinedTextField(
                            value = costPerKm,
                            onValueChange = { costPerKm = it },
                            label = { Text(stringResource(R.string.price_km)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("€") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TCO Section
                    Text(stringResource(R.string.costs_tco), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fuelCost,
                            onValueChange = { fuelCost = it },
                            label = { Text(stringResource(R.string.fuel)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("€") }
                        )

                        OutlinedTextField(
                            value = maintenance,
                            onValueChange = { maintenance = it },
                            label = { Text(stringResource(R.string.maintenance)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("€") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = insurance,
                            onValueChange = { insurance = it },
                            label = { Text(stringResource(R.string.insurance)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("€") }
                        )

                        OutlinedTextField(
                            value = depreciation,
                            onValueChange = { depreciation = it },
                            label = { Text(stringResource(R.string.depreciation)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("€") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Address Section (optional - for updating location)
                    Text(stringResource(R.string.location_optional), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.current_location, String.format("%.4f", uiState.car?.latitude ?: 0.0), String.format("%.4f", uiState.car?.longitude ?: 0.0)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = streetName,
                            onValueChange = { streetName = it },
                            label = { Text(stringResource(R.string.new_street_name)) },
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = houseNumber,
                            onValueChange = { houseNumber = it },
                            label = { Text(stringResource(R.string.house_number)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = postcode,
                            onValueChange = { postcode = it.uppercase() },
                            label = { Text(stringResource(R.string.postcode)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text(stringResource(R.string.country)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.geocodeAddress(streetName, houseNumber, postcode, country)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B6B6B)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = streetName.isNotBlank() && postcode.isNotBlank() && !uiState.isGeocodingInProgress
                    ) {
                        if (uiState.isGeocodingInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.lookup_new_address))
                        }
                    }

                    if (uiState.newLatitude != null && uiState.newLongitude != null &&
                        (uiState.newLatitude != uiState.car?.latitude || uiState.newLongitude != uiState.car?.longitude)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.new_location, String.format("%.4f", uiState.newLatitude), String.format("%.4f", uiState.newLongitude)),
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    if (uiState.geocodingError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.geocodingError!!,
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Update Button
                    Button(
                        onClick = {
                            viewModel.updateCar(
                                brand = brand,
                                model = model,
                                licensePlate = licensePlate,
                                category = selectedCategory,
                                pricePerTimeSlot = pricePerTimeSlot.toDoubleOrNull() ?: 0.0,
                                costPerKm = costPerKm.toDoubleOrNull() ?: 0.0,
                                fuelCost = fuelCost.toDoubleOrNull() ?: 0.0,
                                maintenance = maintenance.toDoubleOrNull() ?: 0.0,
                                insurance = insurance.toDoubleOrNull() ?: 0.0,
                                depreciation = depreciation.toDoubleOrNull() ?: 0.0
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B6B6B)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isUpdating && brand.isNotBlank() && model.isNotBlank()
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Delete Button
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        enabled = !uiState.isDeleting
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Red,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.delete_car), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
