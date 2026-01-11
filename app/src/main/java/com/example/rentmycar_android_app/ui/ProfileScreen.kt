package com.example.rentmycar_android_app.ui

import androidx.compose. foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled. ArrowBack
import androidx.compose. material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx. compose.ui.text.style. TextAlign
import androidx.compose. ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentmycar_android_app.viewmodels. ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    token: String?,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToDrivingStats: () -> Unit = {},  // NIEUW
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val bonus by viewModel.bonus.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(token) {
        viewModel.loadProfile(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Terug"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color. White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Profiel",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pas hier uw persoonlijke gegevens aan",
                fontSize = 14.sp,
                color = Color. Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bonuspunten Card (clickable naar stats)
            if (bonus != null) {
                Card(
                    modifier = Modifier
                        . fillMaxWidth()
                        .clickable { onNavigateToDrivingStats() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6200EA)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier. fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Jouw Bonuspunten",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${bonus?. totalPoints ?: 0}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier. fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Bekijk details",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier. width(4.dp))
                            Text("→", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier. height(16.dp))
            }

            if (loading && user == null) {
                CircularProgressIndicator()
            } else {
                // Naam veld
                Text(
                    text = "Naam",
                    modifier = Modifier.align(Alignment.Start),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier. height(8.dp))
                OutlinedTextField(
                    value = user?.name ?: "",
                    onValueChange = viewModel::onNameChange,
                    placeholder = { Text("Uw naam") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = user != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF6200EA)
                    )
                )

                Spacer(modifier = Modifier. height(16.dp))

                // Telefoon veld
                Text(
                    text = "Phone Number",
                    modifier = Modifier.align(Alignment.Start),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = user?.phone ?: "",
                    onValueChange = viewModel::onPhoneChange,
                    placeholder = { Text("Vul uw telefoon nummer in") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = user != null,
                    leadingIcon = {
                        Text(
                            text = "+31",
                            modifier = Modifier.padding(start = 12.dp),
                            color = Color. Gray
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color. Transparent,
                        focusedBorderColor = Color(0xFF6200EA)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email veld
                Text(
                    text = "email",
                    modifier = Modifier.align(Alignment.Start),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = user?.email ?: "",
                    onValueChange = viewModel::onEmailChange,
                    placeholder = { Text("Uw email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = user != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color. Transparent,
                        focusedBorderColor = Color(0xFF6200EA)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Foutmelding
                if (error != null) {
                    Text(
                        text = error ?: "",
                        color = if (error?. contains("succesvol") == true)
                            Color(0xFF4CAF50)
                        else
                            MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Gegevens opslaan button
                Button(
                    onClick = { viewModel.updateProfile(token) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E8E93)
                    ),
                    enabled = ! loading && user != null
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier. size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Gegevens opslaan",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Uitloggen button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E8E93)
                    )
                ) {
                    Text(
                        text = "Uitloggen",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}