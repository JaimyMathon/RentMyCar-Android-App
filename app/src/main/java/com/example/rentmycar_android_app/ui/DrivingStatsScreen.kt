import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentmycar_android_app.model.DrivingBehavior
import com.example.rentmycar_android_app.model.DrivingStatsResponse
import com.example.rentmycar_android_app.network.ApiClient
import com.example.rentmycar_android_app.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun DrivingStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DrivingStatsViewModel = viewModel(
        factory = DrivingStatsViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadStats() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mijn Ritten",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "Terug", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "Er is een fout opgetreden", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadStats() }) { Text("Opnieuw Proberen") }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.stats?.behaviors?.isEmpty() == true) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color.White, shape = RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nog geen ritten beschikbaar.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    items(uiState.stats?.behaviors ?: emptyList()) { TripCardStyled(it) }
                }
            }
        }
    }
}

@Composable
fun TripCardStyled(behavior: DrivingBehavior) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Auto Naam", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    Text(behavior.carName ?: "Onbekend", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text("${behavior.points} punten", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Divider(color = Color.LightGray, thickness = 1.dp)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Afstand", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    Text("${String.format("%.1f", behavior.distance / 1000)} km", fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Rating", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    Text(behavior.rating ?: "Geen data", fontSize = 16.sp)
                }
            }
        }
    }
}

class DrivingStatsViewModel(private val context: Context) : ViewModel() {
    private val apiService = ApiClient.instance.create(ApiService::class.java)
    private val _uiState = MutableStateFlow(DrivingStatsUiState())
    val uiState: StateFlow<DrivingStatsUiState> = _uiState.asStateFlow()

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val token = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("jwt_token", "")
                ?: ""
            if (token.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Geen geldige token")
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
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Netwerkfout: ${e.message}")
            }
        }
    }
}

data class DrivingStatsUiState(
    val isLoading: Boolean = false,
    val stats: DrivingStatsResponse? = null,
    val error: String? = null
)

class DrivingStatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DrivingStatsViewModel(context) as T
    }
}