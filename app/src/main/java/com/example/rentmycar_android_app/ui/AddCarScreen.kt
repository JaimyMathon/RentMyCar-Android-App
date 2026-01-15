package com.example.rentmycar_android_app.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.rentmycar_android_app.R
import com.example.rentmycar_android_app.data.CarRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "AddCarScreen"

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    onBack: () -> Unit,
    onCarAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val repo = remember { CarRepository(context) }
    val scope = rememberCoroutineScope()

    // Car fields
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var pricePerTimeSlot by remember { mutableStateOf("") }
    var costPerKm by remember { mutableStateOf("") }

    // Address fields
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }

    // Photo
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // UI state
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }

    // String resources for validation
    val fillBrand = stringResource(R.string.fill_brand)
    val fillModel = stringResource(R.string.fill_model)
    val fillLicensePlate = stringResource(R.string.fill_license_plate)
    val fillCategory = stringResource(R.string.fill_category)
    val fillPriceTimeslot = stringResource(R.string.fill_price_timeslot)
    val fillPriceKm = stringResource(R.string.fill_price_km)
    val fillCountry = stringResource(R.string.fill_country)
    val fillCity = stringResource(R.string.fill_city)
    val fillPostalCode = stringResource(R.string.fill_postal_code)
    val fillStreet = stringResource(R.string.fill_street)
    val fillHouseNumber = stringResource(R.string.fill_house_number)
    val selectPhoto = stringResource(R.string.select_photo)
    val carAddedText = stringResource(R.string.car_added)
    val saveFailedText = stringResource(R.string.save_failed)

    // Gallery picker
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) selectedPhotoUri = uri
    }

    // Camera
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    fun createCameraUri(ctx: Context): Uri {
        val imagesDir = File(ctx.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("car_", ".jpg", imagesDir)
        return FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            file
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) {
            showConfirmDialog = true
        } else {
            pendingCameraUri = null
        }
    }

    fun validate(): String? {
        if (brand.isBlank()) return fillBrand
        if (model.isBlank()) return fillModel
        if (licensePlate.isBlank()) return fillLicensePlate
        if (category.isBlank()) return fillCategory
        if (pricePerTimeSlot.isBlank()) return fillPriceTimeslot
        if (costPerKm.isBlank()) return fillPriceKm
        if (country.isBlank()) return fillCountry
        if (city.isBlank()) return fillCity
        if (postalCode.isBlank()) return fillPostalCode
        if (street.isBlank()) return fillStreet
        if (houseNumber.isBlank()) return fillHouseNumber
        if (selectedPhotoUri == null) return selectPhoto
        return null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_car_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF2F5FF))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- Car ---
            FormField(stringResource(R.string.brand_label), brand) { brand = it }
            FormField(stringResource(R.string.model_label), model) { model = it }
            FormField(stringResource(R.string.license_plate_label), licensePlate) { licensePlate = it }
            FormField(stringResource(R.string.category_label), category) { category = it }
            FormField(stringResource(R.string.price_per_timeslot_label), pricePerTimeSlot) { pricePerTimeSlot = it }
            FormField(stringResource(R.string.price_per_km_label), costPerKm) { costPerKm = it }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.address_section), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // --- Address ---
            FormField(stringResource(R.string.country_label), country) { country = it }
            FormField(stringResource(R.string.city_label), city) { city = it }
            FormField(stringResource(R.string.postal_code_label), postalCode) { postalCode = it }
            FormField(stringResource(R.string.street_label), street) { street = it }
            FormField(stringResource(R.string.house_number_label), houseNumber) { houseNumber = it }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.photo_section), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.choose_photo)) }

                Button(
                    onClick = {
                        error = null
                        success = null
                        val uri = createCameraUri(context)
                        pendingCameraUri = uri
                        takePictureLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.take_photo)) }
            }

            Spacer(Modifier.height(8.dp))
            selectedPhotoUri?.let {
                Text(stringResource(R.string.selected_photo, it.toString()), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    Log.d(TAG, "=== SAVE BUTTON CLICKED ===")
                    error = null
                    success = null

                    val validationError = validate()
                    Log.d(TAG, "Validation result: $validationError")
                    if (validationError != null) {
                        error = validationError
                        Log.d(TAG, "Validation failed: $validationError")
                        return@Button
                    }

                    val photo = selectedPhotoUri!!
                    Log.d(TAG, "Photo URI: $photo")

                    loading = true
                    Log.d(TAG, "Starting coroutine to add car...")
                    scope.launch {
                        try {
                            Log.d(TAG, "Calling repo.addCarWithPhoto with: brand=$brand, model=$model, licensePlate=$licensePlate")
                            Log.d(TAG, "Address: $street $houseNumber, $postalCode $city, $country")
                            val res = repo.addCarWithPhoto(
                                brand = brand,
                                model = model,
                                licensePlate = licensePlate,
                                category = category,
                                pricePerTimeSlot = pricePerTimeSlot,
                                costPerKm = costPerKm,
                                country = country,
                                city = city,
                                postalCode = postalCode,
                                street = street,
                                houseNumber = houseNumber,
                                photoUri = photo
                            )
                            Log.d(TAG, "API Response: isSuccess=${res.isSuccess}, message=${res.message}")
                            success = res.message ?: carAddedText
                            Log.d(TAG, "Car added successfully! Waiting before navigation...")
                            // Wacht even zodat gebruiker succes melding kan zien
                            delay(1500)
                            Log.d(TAG, "Calling onCarAdded() to navigate...")
                            onCarAdded()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error adding car: ${e.message}", e)
                            error = e.message ?: saveFailedText
                        } finally {
                            loading = false
                            Log.d(TAG, "Loading state reset to false")
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (loading) stringResource(R.string.saving) else stringResource(R.string.save))
            }

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text(error!!, color = Color.Red)
            }
            if (success != null) {
                Spacer(Modifier.height(10.dp))
                Text(success!!, color = Color(0xFF1B5E20))
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.back)) }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Confirm dialog after camera
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                pendingCameraUri = null
            },
            title = { Text(stringResource(R.string.use_photo_title)) },
            text = { Text(stringResource(R.string.use_photo_message)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingCameraUri?.let { selectedPhotoUri = it }
                    showConfirmDialog = false
                    pendingCameraUri = null
                }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    pendingCameraUri = null
                }) { Text(stringResource(R.string.no)) }
            }
        )
    }
}
