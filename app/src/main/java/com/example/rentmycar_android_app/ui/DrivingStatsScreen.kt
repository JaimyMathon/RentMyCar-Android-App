package com.example.rentmycar_android_app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentmycar_android_app.R
import com.example.rentmycar_android_app.model.DrivingBehavior
import com.example.rentmycar_android_app.model.DrivingStatsResponse
import com.example.rentmycar_android_app.network.ApiClient
import com.example.rentmycar_android_app.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivingStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DrivingStatsViewModel = viewModel(
        factory = DrivingStatsViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadStats() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.driving_stats_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6200EA))
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ ${uiState.error}", color = Color.Red, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadStats() }) { Text(stringResource(R.string.retry)) }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatsHeader(
                        totalPoints = uiState.stats?.totalPoints ?: 0,
                        averageRating = uiState.stats?.averageRating ?: "N/A",
                        totalTrips = uiState.stats?.totalTrips ?: 0,
                        totalDistance = uiState.stats?.totalDistance ?: 0.0
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.scoring_info_title), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1976D2))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.scoring_info_body),
                                fontSize = 12.sp,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }

                item { Text(stringResource(R.string.recent_trips), fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                if (uiState.stats?.behaviors?.isEmpty() == true) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🚗", fontSize = 48.sp)
                                Spacer(Modifier.height(16.dp))
                                Text(stringResource(R.string.no_trips_yet), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.start_your_first_trip), fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(uiState.stats?.behaviors ?: emptyList()) { TripCard(it) }
                }
            }
        }
    }
}

@Composable
private fun StatsHeader(totalPoints: Int, averageRating: String, totalTrips: Int, totalDistance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EA))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.your_score), fontSize = 16.sp, color = Color.White.copy(0.8f))
            Text("$totalPoints", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(stringResource(R.string.points), fontSize = 14.sp, color = Color.White.copy(0.8f))
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(0.3f))
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(stringResource(R.string.average), averageRating, "⭐")
                StatItem(stringResource(R.string.trips), "$totalTrips", "🚗")
                StatItem(stringResource(R.string.distance), "${String.format("%.1f", totalDistance / 1000)} ${stringResource(R.string.unit_km)}", "📍")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.White.copy(0.8f))
    }
}

@Composable
private fun TripCard(behavior: DrivingBehavior) {
    val ratingColor = when (behavior.rating) {
        "EXCELLENT" -> Color(0xFF4CAF50)
        "GOOD" -> Color(0xFF8BC34A)
        "MODERATE" -> Color(0xFFFFC107)
        "POOR" -> Color(0xFFFF9800)
        "DANGEROUS" -> Color(0xFFF44336)
        else -> Color.Gray
    }
    val ratingText = when (behavior.rating) {
        "EXCELLENT" -> stringResource(R.string.rating_excellent)
        "GOOD" -> stringResource(R.string.rating_good)
        "MODERATE" -> stringResource(R.string.rating_moderate)
        "POOR" -> stringResource(R.string.rating_poor)
        "DANGEROUS" -> stringResource(R.string.rating_dangerous)
        else -> behavior.rating
    }
    val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("nl")).format(Date(behavior.timestamp))

    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(behavior.carName ?: "Auto", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(date, fontSize = 12.sp, color = Color.Gray)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = ratingColor.copy(0.2f)) {
                    Text(
                        ratingText,
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = ratingColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE3F2FD)) {
                    Text(
                        "${behavior.points} pts",
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFF1976D2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TripStat(stringResource(R.string.duration, ""), formatDuration(behavior.duration))
                TripStat(stringResource(R.string.distance), "${String.format(Locale.US, "%.2f", behavior.distance / 1000)} ${stringResource(R.string.unit_km)}")
                TripStat(stringResource(R.string.max_speed), "${String.format("%.0f", behavior.maxSpeed * 3.6)} ${stringResource(R.string.unit_km_per_h)}")
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.acceleration), fontSize = 12.sp, color = Color.Gray)
                    Text("Max: ${String.format("%.1f", behavior.maxAccelerationForce)} ${stringResource(R.string.unit_m_s2)}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    if (behavior.harshAccelerationCount > 0) {
                        Text("⚠️ ${behavior.harshAccelerationCount}x ${stringResource(R.string.harsh_acceleration)}", fontSize = 11.sp, color = Color(0xFFFF9800))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.braking), fontSize = 12.sp, color = Color.Gray)
                    Text("Max: ${String.format("%.1f", behavior.maxBrakingForce)} ${stringResource(R.string.unit_m_s2)}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    if (behavior.harshBrakingCount > 0) {
                        Text("⚠️ ${behavior.harshBrakingCount}x ${stringResource(R.string.harsh_braking)}", fontSize = 11.sp, color = Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}

@Composable
private fun TripStat(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = (millis / 1000 / 60) % 60
    val hours = millis / 1000 / 3600
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

// ViewModel
data class DrivingStatsUiState(
    val isLoading: Boolean = false,
    val stats: DrivingStatsResponse? = null,
    val error: String? = null
)

class DrivingStatsViewModel(private val context: Context) : ViewModel() {
    private val apiService = ApiClient.instance.create(ApiService::class.java)
    private val _uiState = MutableStateFlow(DrivingStatsUiState())
    val uiState: StateFlow<DrivingStatsUiState> = _uiState.asStateFlow()

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val token = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("jwt_token", "") ?: ""
            if (token.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = context.getString(R.string.no_token_found))
                return@launch
            }
            try {
                val response = apiService.getDrivingStats("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, stats = response.body())
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Fout: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "${context.getString(R.string.network_error, e.message ?: "")}")
            }
        }
    }
}

class DrivingStatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DrivingStatsViewModel(context) as T
    }
}
