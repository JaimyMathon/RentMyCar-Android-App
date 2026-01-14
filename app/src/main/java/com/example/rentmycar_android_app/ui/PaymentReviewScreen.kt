package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

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
    viewModel: com.example.rentmycar_android_app.ui.payment.PaymentReviewViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            max(1, diffDays) // 04->07 = 3
        } catch (_: Exception) { 1 }
    }

    val days = daysBetween(fromDate, toDate)
    val kmsInt = kms.toIntOrNull() ?: 0

    val currency = remember { NumberFormat.getCurrencyInstance(Locale("nl", "NL")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Betaal overzicht") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
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
                    onClick = { viewModel.processPayment() },
                    enabled = car != null && !uiState.isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Betalen", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(top = 40.dp))
                uiState.error != null -> Text(
                    uiState.error!!,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 40.dp)
                )

                uiState.car != null -> {
                    val car = uiState.car!!

                    val pricePerDay = car.pricePerTimeSlot ?: 0.0
                    val basePrice = pricePerDay * days

                    // Zoals jouw voorbeeld: TCO / 365 * dagen
                    val tcoAnnual = car.tco ?: 0.0
                    val tcoCost = (tcoAnnual / 365.0) * days

                    val costPerKm = car.costPerKm ?: 0.0
                    val distanceCost = kmsInt * costPerKm

                    val total = basePrice + tcoCost + distanceCost
                    val totalHours = days * 24

                    Column(modifier = Modifier.fillMaxWidth()) {

                        Spacer(Modifier.height(12.dp))

                        // Header auto
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

                        // Datums
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ophaal datum", color = Color.Gray)
                            Text(fromDate, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Terug breng datum", color = Color.Gray)
                            Text(toDate, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(16.dp))
                        Divider()
                        Spacer(Modifier.height(16.dp))

                        // Totale uren
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Totale uren", color = Color.Gray)
                            Text("$totalHours", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Basis huurprijs
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Basis huurprijs", color = Color.Gray)
                                Text("$days x ${currency.format(pricePerDay)}", color = Color.Gray)
                            }
                            Text(currency.format(basePrice), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(12.dp))

                        // TCO
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("TCO-kosten", color = Color.Gray)
                                Text("${currency.format(tcoAnnual)} : 365 x $days", color = Color.Gray)
                            }
                            Text(currency.format(tcoCost), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Afstand
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Afstand kosten", color = Color.Gray)
                                Text("${kmsInt}km x ${currency.format(costPerKm)}", color = Color.Gray)
                            }
                            Text(currency.format(distanceCost), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(16.dp))
                        Divider()
                        Spacer(Modifier.height(16.dp))

                        // Totaal
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Totaal", fontWeight = FontWeight.Bold)
                            Text(currency.format(total), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}