package com.example.rentmycar_android_app.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.CarService
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.network.ReservationService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

// ============================================================================
// DESIGN PATTERN: State Container + Use Cases (Single File)
// ============================================================================

/**
 * State holder voor ReservationScreen
 * Volgt het "State Hoisting" pattern
 */
data class ReservationState(
    val carName: String? = null,
    val reservedPeriods: List<ReservationDto> = emptyList(),
    val fromDate: String = "",
    val toDate: String = "",
    val kilometers: String = "",
    val isLoadingReservations: Boolean = true,
    val error: String? = null
) {
    val isFormValid: Boolean
        get() = fromDate.isNotBlank() && toDate.isNotBlank() && kilometers.isNotBlank()
}

/**
 * Use Case: Laad car details en reserveringen
 * Volgt Single Responsibility Principle
 */
class LoadCarDataUseCase(
    private val carService: CarService,
    private val reservationService: ReservationService
) {
    suspend fun execute(carId: String): Result<Pair<String?, List<ReservationDto>>> {
        return try {
            // Fetch car name
            val carName = try {
                val car = carService.getCarById(carId)
                "${car.brand} ${car.model}"
            } catch (_: Exception) {
                null
            }

            // Fetch reservations
            val reservations = try {
                reservationService.getReservationsByCarId(carId)
                    .filter { it.status == "pending" || it.status == "confirmed" }
            } catch (_: Exception) {
                emptyList()
            }

            Result.success(Pair(carName, reservations))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Helper voor datum formatting
 * Volgt Single Responsibility Principle
 */
object DateFormatter {
    fun formatReservationDate(isoDate: String?): String {
        if (isoDate == null) return ""
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoDate)
            zonedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } catch (_: Exception) {
            isoDate
        }
    }
}

/**
 * Main composable met state management
 */
@Composable
fun ReservationScreen(
    carId: String,
    onBackClick: () -> Unit = {},
    onContinueClick: (fromDate: String, toDate: String, kms: String) -> Unit = { _, _, _ -> }
) {
    // State hoisting - alle state in één plek
    var state by remember { mutableStateOf(ReservationState()) }

    val screenBg = Color(0xFFF6F5F5)
    val fieldBg = Color(0xFFF0EBEB)

    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    // Initialize services and use case
    val apiClient = remember { ApiClientWithToken(context) }
    val carService = remember { apiClient.instance.create(CarService::class.java) }
    val reservationService = remember { apiClient.instance.create(ReservationService::class.java) }
    val loadCarDataUseCase = remember { LoadCarDataUseCase(carService, reservationService) }

    // Fetch data on mount
    LaunchedEffect(carId) {
        state = state.copy(isLoadingReservations = true)

        scope.launch {
            loadCarDataUseCase.execute(carId)
                .onSuccess { (carName, reservations) ->
                    state = state.copy(
                        carName = carName,
                        reservedPeriods = reservations,
                        isLoadingReservations = false
                    )
                }
                .onFailure { error ->
                    state = state.copy(
                        error = "Kan gegevens niet laden: ${error.message}",
                        isLoadingReservations = false
                    )
                }
        }
    }

    // Event handlers
    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val formatted = dateFormat.format(cal.time)
                onDateSelected(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // UI
    Scaffold(
        backgroundColor = screenBg,
        topBar = {
            TopAppBar(
                backgroundColor = screenBg,
                elevation = 0.dp,
                title = {
                    Text(
                        text = "Reserveer auto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(screenBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onContinueClick(state.fromDate, state.toDate, state.kilometers) },
                    enabled = state.isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (state.isFormValid) Color(0xFF6B6B6B) else Color(0xFFC5C0C0),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Doorgaan")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            // Auto naam tonen
            if (!state.carName.isNullOrBlank()) {
                Text(
                    text = state.carName!!,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Uw Informatie details",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Vanaf wanneer tot wanneer wilt u de auto huren?",
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show reserved periods
            ReservationStatusSection(
                isLoading = state.isLoadingReservations,
                reservedPeriods = state.reservedPeriods
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReservationDropdownField(
                label = "Reserveer vanaf",
                value = state.fromDate,
                placeholder = "Datum",
                background = fieldBg,
                onClick = { showDatePicker { date -> state = state.copy(fromDate = date) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReservationDropdownField(
                label = "Reserveer tot en met",
                value = state.toDate,
                placeholder = "Datum",
                background = fieldBg,
                onClick = { showDatePicker { date -> state = state.copy(toDate = date) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aantal Km's",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = state.kilometers,
                onValueChange = { state = state.copy(kilometers = it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(text = "Bv: 300", color = Color(0xFFB2AAAA)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = fieldBg,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    textColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!state.isFormValid) {
                Text(
                    text = "Vul alle velden in om door te gaan.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

/**
 * Extracted composable voor reservering status
 * Volgt compositie pattern
 */
@Composable
private fun ReservationStatusSection(
    isLoading: Boolean,
    reservedPeriods: List<ReservationDto>
) {
    when {
        isLoading -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF6F6A6A)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Beschikbaarheid laden...",
                    fontSize = 12.sp,
                    color = Color(0xFF6F6A6A)
                )
            }
        }

        reservedPeriods.isNotEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFFFF3CD),
                shape = RoundedCornerShape(8.dp),
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Let op: Deze auto is al gereserveerd op:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF856404)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    reservedPeriods.forEach { reservation ->
                        val startFormatted = DateFormatter.formatReservationDate(reservation.startTime)
                        val endFormatted = DateFormatter.formatReservationDate(reservation.endTime)
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF856404), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$startFormatted t/m $endFormatted",
                                fontSize = 12.sp,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationDropdownField(
    label: String,
    value: String,
    placeholder: String,
    background: Color,
    onClick: () -> Unit
) {
    Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(6.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(background, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (value.isBlank()) placeholder else value,
                fontSize = 14.sp,
                color = if (value.isBlank()) Color(0xFFB2AAAA) else Color.Black
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFE0DADA), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Selecteer datum",
                    tint = Color(0xFF5A5555)
                )
            }
        }
    }
}