package com.example.rentmycar_android_app.ui.addcar

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    onBackClick: () -> Unit,
    onCarAdded: () -> Unit,
    viewModel: AddCarViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Form state
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

    // Photo state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Photo picker
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    // Camera
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = tempPhotoUri
        }
    }

    // Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
            tempPhotoUri?.let { takePicture.launch(it) }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCarAdded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Toevoegen", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Photo Section
            Text("Foto", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDE7E7))
                    .border(2.dp, Color(0xFFDCD3D3), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Geselecteerde foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Geen foto geselecteerd", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        pickMedia.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B6B6B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galerij")
                }

                Button(
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B6B6B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Car Details Section
            Text("Auto Gegevens", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Merk") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it.uppercase() },
                label = { Text("Kenteken") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Selection
            Text("Categorie", fontSize = 14.sp, color = Color.Gray)
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
            Text("Prijzen", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = pricePerTimeSlot,
                    onValueChange = { pricePerTimeSlot = it },
                    label = { Text("Prijs/dag") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("€") }
                )

                OutlinedTextField(
                    value = costPerKm,
                    onValueChange = { costPerKm = it },
                    label = { Text("Prijs/km") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("€") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TCO Section
            Text("Kosten (TCO)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fuelCost,
                    onValueChange = { fuelCost = it },
                    label = { Text("Brandstof") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("€") }
                )

                OutlinedTextField(
                    value = maintenance,
                    onValueChange = { maintenance = it },
                    label = { Text("Onderhoud") },
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
                    label = { Text("Verzekering") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("€") }
                )

                OutlinedTextField(
                    value = depreciation,
                    onValueChange = { depreciation = it },
                    label = { Text("Afschrijving") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("€") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Address Section
            Text("Locatie", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = streetName,
                    onValueChange = { streetName = it },
                    label = { Text("Straatnaam") },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = houseNumber,
                    onValueChange = { houseNumber = it },
                    label = { Text("Nr.") },
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
                    label = { Text("Postcode") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Land") },
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
                    Text("Adres Opzoeken")
                }
            }

            if (uiState.latitude != null && uiState.longitude != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Locatie gevonden: ${String.format("%.4f", uiState.latitude)}, ${String.format("%.4f", uiState.longitude)}",
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

            // Submit Button
            Button(
                onClick = {
                    viewModel.addCar(
                        brand = brand,
                        model = model,
                        licensePlate = licensePlate,
                        category = selectedCategory,
                        pricePerTimeSlot = pricePerTimeSlot.toDoubleOrNull() ?: 0.0,
                        costPerKm = costPerKm.toDoubleOrNull() ?: 0.0,
                        fuelCost = fuelCost.toDoubleOrNull() ?: 0.0,
                        maintenance = maintenance.toDoubleOrNull() ?: 0.0,
                        insurance = insurance.toDoubleOrNull() ?: 0.0,
                        depreciation = depreciation.toDoubleOrNull() ?: 0.0,
                        photoUri = selectedImageUri,
                        context = context
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading && brand.isNotBlank() && model.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Auto Toevoegen", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
