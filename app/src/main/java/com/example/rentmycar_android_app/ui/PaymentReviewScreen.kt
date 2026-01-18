package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.rentmycar_android_app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmycar_android_app.ui.payment.PaymentReviewUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

/**
 * Wrapper composable die de state uit de (Hilt) ViewModel haalt.
 * UI tests richten zich op PaymentReviewScreenContent zodat Hilt niet nodig is in tests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReviewScreen(
    carId: String,
    fromDate: String,
    toDate: String,
    kms: String,
    paymentMethod: String,
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: com.example.rentmycar_android_app.ui.payment.PaymentReviewViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PaymentReviewScreenContent(
        carId = carId,
        fromDate = fromDate,
        toDate = toDate,
        kms = kms,
        paymentMethod = paymentMethod,
        uiState = uiState,
        onBackClick = onBackClick,
        onPaymentSuccess = onPaymentSuccess,
        onPayClick = { viewModel.processPayment() }
    )
}

/**
 * UI-only composable. Hierin zit alle render logic.
 * Deze is testbaar zonder Hilt / netwerk / repositories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReviewScreenContent(
    carId: String,
    fromDate: String,
    toDate: String,
    kms: String,
    paymentMethod: String,
    uiState: PaymentReviewUiState,
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onPayClick: () -> Unit
) {
    // Handle payment success
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            onPaymentSuccess()
        }
    }

    val df = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    fun daysBetween(from: String, to: String): Int {
        return try {
            val f = df.parse(from)!!.time
            val t = df.parse(to)!!.time
            val diffDays = ((t - f) / (1000L * 60L * 60L * 24L)).toInt()
            max(1, diffDays)
        } catch (_: Exception) {
            1
        }
    }

    val days = daysBetween(fromDate, toDate)
    val kmsInt = kms.toIntOrNull() ?: 0
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("nl", "NL")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.payment_overview)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            val car = uiState.car
            val buttonColor = Color(0xFF6B6B6B)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onPayClick,
                    enabled = car != null && !uiState.isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("payment_pay_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("payment_processing"),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            stringResource(R.string.pay),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .testTag("payment_loading")
                )

                uiState.error != null -> Text(
                    uiState.error!!,
                    color = Color.Red,
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .testTag("payment_error")
                )

                uiState.car != null -> {
                    val car = uiState.car!!

                    val pricePerDay = car.pricePerTimeSlot ?: 0.0
                    val basePrice = pricePerDay * days

                    val tcoAnnual = car.tco ?: 0.0
                    val tcoCost = (tcoAnnual / 365.0) * days

                    val costPerKm = car.costPerKm ?: 0.0
                    val distanceCost = kmsInt * costPerKm

                    val total = basePrice + tcoCost + distanceCost
                    val totalHours = days * 24

                    Column(modifier = Modifier.fillMaxWidth()) {

                        Spacer(Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${car.brand.orEmpty()} ${car.model.orEmpty()}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${currency.format(pricePerDay)}/dag",
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.pickup_date), color = Color.Gray)
                            Text(fromDate, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.return_date), color = Color.Gray)
                            Text(toDate, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(16.dp))
                        Divider()
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.total_hours), color = Color.Gray)
                            Text("$totalHours", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(stringResource(R.string.base_rental_price), color = Color.Gray)
                                Text("$days x ${currency.format(pricePerDay)}", color = Color.Gray)
                            }
                            Text(currency.format(basePrice), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(stringResource(R.string.tco_costs), color = Color.Gray)
                                Text("${currency.format(tcoAnnual)} : 365 x $days", color = Color.Gray)
                            }
                            Text(currency.format(tcoCost), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(stringResource(R.string.distance_costs), color = Color.Gray)
                                Text("${kmsInt}km x ${currency.format(costPerKm)}", color = Color.Gray)
                            }
                            Text(currency.format(distanceCost), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(16.dp))
                        Divider()
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.total), fontWeight = FontWeight.Bold)
                            Text(currency.format(total), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}