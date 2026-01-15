package com.example.rentmycar_android_app.ui.mycars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CarService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCarsScreen(
    onBackClick: () -> Unit,
    onCarClick: (String) -> Unit,
    onAddCarClick: () -> Unit,
    viewModel: MyCarsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh when screen becomes visible again (after navigating back)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mijn Auto's", fontWeight = FontWeight.SemiBold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCarClick,
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Auto toevoegen", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Fout: ${uiState.error}",
                            color = Color.Red
                        )
                    }
                }

                uiState.cars.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Je hebt nog geen auto's toegevoegd",
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Klik op + om een auto toe te voegen",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 90.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.cars) { car ->
                            MyCarCard(
                                car = car,
                                onClick = {
                                    if (car.id.isNotBlank()) {
                                        onCarClick(car.id)
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
private fun MyCarCard(
    car: CarDto,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var photoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(car.id) {
        if (car.id.isNotBlank()) {
            try {
                val carService = ApiClientWithToken(context).instance.create(CarService::class.java)
                val photos = carService.getCarPhotos(car.id)
                if (photos.isNotEmpty()) {
                    photoUrl = "http://10.0.2.2:8080${photos[0].url}"
                }
            } catch (e: Exception) {
                android.util.Log.e("MyCarCard", "Failed to load car photo: ${e.message}")
            }
        }
    }

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
                if (photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Auto foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "€${(car.pricePerTimeSlot ?: 0.0).toInt()}/dag",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = car.status ?: "Beschikbaar",
                    fontSize = 13.sp,
                    color = if (car.status == "AVAILABLE") Color(0xFF4CAF50) else Color.Gray
                )
            }

            if (car.licensePlate != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = car.licensePlate,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
