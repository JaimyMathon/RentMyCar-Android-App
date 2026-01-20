package com.example.rentmycar_android_app.ui

import android. Manifest
import android.content.Context
import android.content.pm. PackageManager
import android.location.Location
import android.os. Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract. ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose. material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose. ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose. ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.rentmycar_android_app.R
import androidx.core.content.ContextCompat
import androidx.lifecycle. ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle. viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentmycar_android_app.model.DrivingDataRequestBuilder
import com.example.rentmycar_android_app.network.ApiClient
import com.example.rentmycar_android_app.network.ApiService
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import kotlinx.coroutines. flow.MutableStateFlow
import kotlinx. coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.rentmycar_android_app.scoring.ScoringStrategy
import com.example.rentmycar_android_app.scoring.StandardScoringStrategy
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivingTrackerScreen(
    onNavigateBack: () -> Unit,
    viewModel: DrivingTrackerViewModel = viewModel(
        factory = DrivingTrackerViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts. RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) viewModel.onPermissionsGranted()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission. ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            viewModel.onPermissionsGranted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.driving_tracker), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6200EA))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                . fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier. fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isTracking) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (uiState.isTracking) "🚗 ${stringResource(R.string.driving)}" else "⏸️ ${stringResource(R.string.stopped)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(stringResource(R.string.duration, formatDuration(uiState.duration)), fontSize = 16.sp, color = Color. White)
                    Text(stringResource(R.string.distance, String.format("%.2f", uiState. distance / 1000)), fontSize = 14.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier. height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.acceleration),
                    value = String.format("%.1f", abs(uiState.currentAcceleration)),
                    max = String.format("%.1f", uiState.maxAcceleration),
                    count = uiState.harshAccelerationCount,
                    color = Color(0xFF4CAF50)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.braking),
                    value = String.format("%.1f", abs(uiState.currentBraking)),
                    max = String.format("%.1f", uiState.maxBraking),
                    count = uiState.harshBrakingCount,
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.message != null) {
                Text(
                    uiState.message ?:  "",
                    color = if (uiState.message?. contains("✅") == true) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = { if (uiState. isTracking) viewModel.stopAndSave() else viewModel.startTracking() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isTracking) Color(0xFFF44336) else Color(0xFF4CAF50)
                ),
                enabled = uiState. hasLocationPermission && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(if (uiState. isTracking) stringResource(R.string.stop_and_save) else stringResource(R.string.start_trip_button), fontSize = 18.sp, fontWeight = FontWeight. Bold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, title: String, value: String, max: String, count: Int, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(stringResource(R.string.ms_squared), fontSize = 10.sp, color = Color.Gray)
            Text(stringResource(R.string.max_value, max), fontSize = 10.sp, color = Color.Gray)
            Text(stringResource(R.string.harsh_count, count), fontSize = 10.sp, color = if (count > 3) Color.Red else Color.Gray)
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000 / 60) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

data class DrivingTrackerUiState(
    val hasLocationPermission: Boolean = false,
    val isTracking: Boolean = false,
    val currentSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val currentAcceleration: Double = 0.0,
    val currentBraking: Double = 0.0,
    val maxAcceleration: Double = 0.0,
    val maxBraking: Double = 0.0,
    val harshAccelerationCount: Int = 0,
    val harshBrakingCount: Int = 0,
    val distance: Double = 0.0,
    val duration: Long = 0L,
    val isSaving:  Boolean = false,
    val message: String? = null
)

class DrivingTrackerViewModel(private val context: Context) : ViewModel() {
    private val apiService = ApiClient.instance. create(ApiService::class.java)
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback?  = null
    private val _uiState = MutableStateFlow(DrivingTrackerUiState())
    val uiState: StateFlow<DrivingTrackerUiState> = _uiState.asStateFlow()

    private var scoringStrategy: ScoringStrategy = StandardScoringStrategy()

    private var startTime = 0L
    private var lastLocation: Location? = null
    private var lastSpeed = 0.0
    private var speedSum = 0.0
    private var speedCount = 0
    private var lastUpdateTime = 0L

    fun onPermissionsGranted() {
        _uiState.value = _uiState.value.copy(hasLocationPermission = true)
    }

    fun startTracking() {
        startTime = System.currentTimeMillis()
        lastUpdateTime = startTime
        _uiState.value = DrivingTrackerUiState(hasLocationPermission = true, isTracking = true)
        startLocationUpdates()
        viewModelScope.launch {
            while (_uiState.value.isTracking) {
                delay(1000)
                _uiState. value = _uiState.value.copy(duration = System.currentTimeMillis() - startTime)
            }
        }
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { processLocation(it) }
            }
        }
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
            }
        } catch (e: SecurityException) {}
    }

    private fun processLocation(location: Location) {
        val currentSpeed = if (location.hasSpeed()) location.speed. toDouble() else 0.0
        val currentTime = System.currentTimeMillis()

        lastLocation?. let {
            _uiState.value = _uiState.value.copy(distance = _uiState.value.distance + it.distanceTo(location))
        }

        if (lastSpeed > 0 && lastUpdateTime > 0) {
            val timeDiffSeconds = (currentTime - lastUpdateTime) / 1000.0

            if (timeDiffSeconds > 0.1) {
                val speedDiff = currentSpeed - lastSpeed
                val acceleration = speedDiff / timeDiffSeconds

                if (abs(acceleration) > 0.5) {
                    if (acceleration > 0) {
                        _uiState.value = _uiState.value. copy(
                            currentAcceleration = acceleration,
                            currentBraking = 0.0,
                            maxAcceleration = maxOf(_uiState.value.maxAcceleration, acceleration),
                            harshAccelerationCount = if (acceleration > 5.0)
                                _uiState. value.harshAccelerationCount + 1
                            else
                                _uiState.value.harshAccelerationCount
                        )
                    } else {
                        val braking = abs(acceleration)
                        _uiState. value = _uiState.value.copy(
                            currentAcceleration = 0.0,
                            currentBraking = braking,
                            maxBraking = maxOf(_uiState.value.maxBraking, braking),
                            harshBrakingCount = if (braking > 5.0)
                                _uiState. value.harshBrakingCount + 1
                            else
                                _uiState.value.harshBrakingCount
                        )
                    }
                }
            }
        }

        speedSum += currentSpeed
        speedCount++
        _uiState.value = _uiState.value.copy(
            currentSpeed = currentSpeed,
            maxSpeed = maxOf(_uiState.value.maxSpeed, currentSpeed),
            avgSpeed = speedSum / speedCount
        )

        lastSpeed = currentSpeed
        lastLocation = location
        lastUpdateTime = currentTime
    }

    fun stopAndSave() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        val duration = System.currentTimeMillis() - startTime

        if (duration < 5000) {
            _uiState.value = _uiState.value.copy(isTracking = false, message = context.getString(R.string.trip_too_short))
            return
        }

        _uiState.value = _uiState.value.copy(isTracking = false, isSaving = true)

        viewModelScope.launch {
            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("jwt_token", "") ?: ""

            android.util.Log.d("DrivingTracker", "Token: ${if (token.isEmpty()) "EMPTY" else "Found (${token.length} chars)"}")

            if (token.isEmpty()) {
                _uiState.value = _uiState.value.copy(isSaving = false, message = context.getString(R.string.no_token_found))
                return@launch
            }

            try {
                val localScore = scoringStrategy.calculateLocalScore(
                    _uiState.value.maxAcceleration,
                    _uiState.value.maxBraking,
                    _uiState. value.harshAccelerationCount,
                    _uiState. value.harshBrakingCount
                )
                android.util.Log.d("DrivingTracker", "Local calculated score: $localScore")

                val request = DrivingDataRequestBuilder()
                    .setMaxAccelerationForce(_uiState.value.maxAcceleration)
                    .setMaxBrakingForce(_uiState.value.maxBraking)
                    .setAvgSpeed(_uiState.value.avgSpeed)
                    .setMaxSpeed(_uiState.value.maxSpeed)
                    .setDistance(_uiState.value.distance)
                    .setDuration(duration)
                    .setHarshAccelerationCount(_uiState.value.harshAccelerationCount)
                    .setHarshBrakingCount(_uiState.value.harshBrakingCount)
                    .setCarName(context.getString(R.string.test_car))
                    .build()

                val response = apiService.saveDrivingBehavior("Bearer $token", request)

                if (response.isSuccessful && response.body() != null) {
                    val b = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        message = "✅ ${context.getString(R.string.points_earned, b.points)}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isSaving = false, message = "❌ ${context.getString(R.string.error_with_message, response.message())}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, message = "❌ ${e.message}")
            }
        }
    }
}

class DrivingTrackerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DrivingTrackerViewModel(context) as T
    }
}