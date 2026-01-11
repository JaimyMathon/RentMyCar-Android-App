package com.example.rentmycar_android_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PaymentMethod {
    Paypal, ApplePay, GooglePay
}

@Composable
fun PaymentMethodScreen(
    onBackClick: () -> Unit,
    onPaymentSelected: (PaymentMethod) -> Unit
) {
    val screenBg = Color(0xFFF6F5F5)
    val cardBg = Color(0xFFF0EBEB)

    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    Scaffold(
        backgroundColor = screenBg,
        topBar = {
            TopAppBar(
                backgroundColor = screenBg,
                elevation = 0.dp,
                title = {
                    Text("Betaal optie", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(screenBg)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    enabled = selectedMethod != null,
                    onClick = {
                        selectedMethod?.let { onPaymentSelected(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("naar betaal overzicht")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp)
        ) {

            Text("Betaal opties", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            PaymentOptionRow("Paypal", cardBg, selectedMethod == PaymentMethod.Paypal) {
                selectedMethod = PaymentMethod.Paypal
            }

            PaymentOptionRow("Apple Pay", cardBg, selectedMethod == PaymentMethod.ApplePay) {
                selectedMethod = PaymentMethod.ApplePay
            }

            PaymentOptionRow("Google Pay", cardBg, selectedMethod == PaymentMethod.GooglePay) {
                selectedMethod = PaymentMethod.GooglePay
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    label: String,
    background: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(background, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}