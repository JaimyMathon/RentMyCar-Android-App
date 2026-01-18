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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmycar_android_app.R

enum class PaymentMethod {
    Paypal, ApplePay, GooglePay
}

@Composable
fun PaymentMethodScreen(
    onBackClick: () -> Unit,
    onContinueClick: (PaymentMethod) -> Unit
) {
    val screenBg = Color(0xFFF6F5F5)
    val cardBg = Color(0xFFF0EBEB)
    val primaryColor = Color(0xFF6B6B6B)

    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    Scaffold(
        backgroundColor = screenBg,
        topBar = {
            TopAppBar(
                backgroundColor = screenBg,
                elevation = 0.dp,
                title = {
                    Text(stringResource(R.string.payment_option), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                        selectedMethod?.let { onContinueClick(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = primaryColor)
                ) {
                    Text(stringResource(R.string.to_payment_overview), color = Color.White)
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
            Text(stringResource(R.string.payment_options), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            PaymentOptionRow(
                label = "Paypal",
                background = cardBg,
                selected = selectedMethod == PaymentMethod.Paypal,
                onClick = { selectedMethod = PaymentMethod.Paypal }
            )

            Spacer(Modifier.height(8.dp))

            PaymentOptionRow(
                label = "Apple Pay",
                background = cardBg,
                selected = selectedMethod == PaymentMethod.ApplePay,
                onClick = { selectedMethod = PaymentMethod.ApplePay }
            )

            Spacer(Modifier.height(8.dp))

            PaymentOptionRow(
                label = "Google Pay",
                background = cardBg,
                selected = selectedMethod == PaymentMethod.GooglePay,
                onClick = { selectedMethod = PaymentMethod.GooglePay }
            )
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