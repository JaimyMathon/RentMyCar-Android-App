package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentmycar_android_app.R
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.ApiService
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CarService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: String,
    onBackClick: () -> Unit,
    onReserveClick: (String) -> Unit
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var car by remember { mutableStateOf<CarDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var ownerName by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(carId) {
        isLoading = true
        error = null
        car = null
        photoUrl = null
        ownerName = null

        try {
            val apiClient = ApiClientWithToken(context).instance
            val carService = apiClient.create(CarService::class.java)
            val apiService = apiClient.create(ApiService::class.java)

            car = carService.getCarById(carId)

            // Fetch car photo
            try {
                val photos = carService.getCarPhotos(carId)
                if (photos.isNotEmpty()) {
                    photoUrl = "http://10.0.2.2:8080${photos[0].url}"
                }
            } catch (e: Exception) {
                android.util.Log.e("CarDetailScreen", "Failed to load car photo: ${e.message}")
            }

            // Fetch owner name
            car?.addedBy?.let { ownerId ->
                try {
                    val users = apiService.getUsers()
                    val owner = users.find { it.id == ownerId }
                    ownerName = owner?.name
                } catch (e: Exception) {
                    android.util.Log.e("CarDetailScreen", "Failed to load owner: ${e.message}")
                }
            }
        } catch (e: Exception) {
            error = e.message ?: context.getString(R.string.error_fetching_car)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.car_details_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            if (car != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
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
                                text = stringResource(R.string.price),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = stringResource(R.string.price_per_day, (car!!.pricePerTimeSlot ?: 0.0).toInt()),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { onReserveClick(carId) },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B6B6B)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.reserve_now),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(error!!, color = Color.Red)
                    }
                }
                car != null -> {
                    val c = car!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Car Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .background(Color(0xFFE8E8E8)),
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
                                // Placeholder icon - box with image symbol
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .border(
                                            width = 3.dp,
                                            color = Color(0xFF666666),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "\uD83D\uDDBC",
                                        fontSize = 32.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }
                        }

                        // Car Name
                        Text(
                            text = "${c.brand.orEmpty()} ${c.model.orEmpty()}",
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Tabs
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier.padding(top = 8.dp),
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = { Text(stringResource(R.string.about)) }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = { Text(stringResource(R.string.gallery_tab)) }
                            )
                        }

                        // Tab Content
                        when (selectedTabIndex) {
                            0 -> OverTabContent(car = c, ownerName = ownerName)
                            1 -> GalleryTabContent(carId = carId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverTabContent(car: CarDto, ownerName: String?) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Owner Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = ownerName ?: stringResource(R.string.unknown),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.owner),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Car Details
        DetailRow(label = stringResource(R.string.brand), value = car.brand ?: "-")
        DetailRow(label = stringResource(R.string.model), value = car.model ?: "-")
        DetailRow(label = stringResource(R.string.price_per_km_label), value = "€${car.costPerKm ?: 0.0}")
        DetailRow(label = stringResource(R.string.tco), value = "€${(car.tco ?: 0.0).toInt()}")
        DetailRow(label = stringResource(R.string.category), value = car.category ?: "-")
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Text(
        text = "$label: $value",
        fontSize = 16.sp,
        color = Color.DarkGray,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun GalleryTabContent(carId: String) {
    val context = LocalContext.current
    var photoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(carId) {
        isLoading = true
        try {
            val service = ApiClientWithToken(context).instance.create(CarService::class.java)
            val photos = service.getCarPhotos(carId)
            photoUrls = photos.map { "http://10.0.2.2:8080${it.url}" }
        } catch (e: Exception) {
            android.util.Log.e("GalleryTab", "Failed to load photos: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            photoUrls.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_photos_available),
                    color = Color.Gray
                )
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    photoUrls.forEach { url ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.car_photo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}