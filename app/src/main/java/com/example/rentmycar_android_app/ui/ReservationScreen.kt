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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReservationScreen(
    carId: String,
    onBackClick: () -> Unit = {},
    onContinueClick: (fromDate: String, toDate: String, kms: String) -> Unit = { _, _, _ -> }
) {
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var kms by remember { mutableStateOf("") }

    val isFormValid = fromDate.isNotBlank() && toDate.isNotBlank() && kms.isNotBlank()

    val screenBg = Color(0xFFF6F5F5)
    val fieldBg = Color(0xFFF0EBEB)

    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    // Auto naam ophalen
    val carService = remember {
        ApiClientWithToken(context).instance.create(CarService::class.java)
    }
    var carName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(carId) {
        carName = try {
            val car = carService.getCarById(carId)
            "${car.brand} ${car.model}"
        } catch (_: Exception) {
            null
        }
    }

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
                    onClick = { onContinueClick(fromDate, toDate, kms) },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isFormValid) Color(0xFF6F6A6A) else Color(0xFFC5C0C0),
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
            if (!carName.isNullOrBlank()) {
                Text(
                    text = carName!!,
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

            Spacer(modifier = Modifier.height(24.dp))

            ReservationDropdownField(
                label = "Reserveer vanaf",
                value = fromDate,
                placeholder = "Datum",
                background = fieldBg,
                onClick = { showDatePicker { fromDate = it } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReservationDropdownField(
                label = "Reserveer tot en met",
                value = toDate,
                placeholder = "Datum",
                background = fieldBg,
                onClick = { showDatePicker { toDate = it } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aantal Km's",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = kms,
                onValueChange = { kms = it },
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

            if (!isFormValid) {
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