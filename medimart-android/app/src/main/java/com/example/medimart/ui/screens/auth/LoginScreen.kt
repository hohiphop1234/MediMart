package com.example.medimart.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartOrangeSoft
import com.example.medimart.theme.MediMartDisabledContent
import com.example.medimart.theme.MediMartDisabledSurface
import com.example.medimart.theme.MediMartTextPrimary
import com.example.medimart.theme.MediMartTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToOtp: (String) -> Unit,
    viewModel: AuthViewModel
) {
    var phone by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val canContinue = !isLoading && phone.length >= 10

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onNavigateToOtp(phone)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MediMartOrangeSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Medi\nMart", 
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), 
                color = MediMartOrange
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text("Đăng nhập", style = MaterialTheme.typography.headlineMedium.copy(color = MediMartTextPrimary))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Nhập số điện thoại để tiếp tục", color = MediMartTextSecondary, style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = { value -> phone = value.filter(Char::isDigit).take(11) },
            label = { Text("Số điện thoại") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (canContinue) viewModel.login(phone) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MediMartOrange,
                focusedLabelColor = MediMartOrange
            )
        )
        
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { viewModel.login(phone) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Premium button height
            shape = CircleShape, // Pill shaped button
            colors = ButtonDefaults.buttonColors(
                containerColor = MediMartOrange,
                disabledContainerColor = MediMartDisabledSurface,
                disabledContentColor = MediMartDisabledContent
            ),
            enabled = canContinue
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MediMartDisabledContent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    "Tiếp tục",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (canContinue) Color.White else MediMartDisabledContent
                )
            }
        }
    }
}
