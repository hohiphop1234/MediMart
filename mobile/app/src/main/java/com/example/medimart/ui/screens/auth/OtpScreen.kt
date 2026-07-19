package com.example.medimart.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medimart.theme.MediMartDisabledContent
import com.example.medimart.theme.MediMartDisabledSurface
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartTextPrimary
import com.example.medimart.theme.MediMartTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    email: String,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    var otp by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val otpSuccess by viewModel.otpSuccess.collectAsState()
    val canVerify = !isLoading && otp.length == 6

    LaunchedEffect(otpSuccess) {
        if (otpSuccess) {
            onLoginSuccess()
            viewModel.consumeOtpSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Nhập mã OTP", style = MaterialTheme.typography.headlineMedium.copy(color = MediMartTextPrimary))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Mã xác thực 6 số đã được gửi tới:\n$email",
            color = MediMartTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it.filter(Char::isDigit).take(6) },
            label = { Text("Mã OTP") },
            placeholder = { Text("• • • • • •") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.72f),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                letterSpacing = 5.sp,
                fontWeight = FontWeight.Bold,
                color = MediMartOrange
            ),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MediMartOrange)
        )
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { viewModel.verifyOtp(email, otp) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MediMartOrange,
                disabledContainerColor = MediMartDisabledSurface,
                disabledContentColor = MediMartDisabledContent
            ),
            enabled = canVerify
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MediMartDisabledContent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Xác thực", style = MaterialTheme.typography.titleMedium, color = if (canVerify) Color.White else MediMartDisabledContent)
            }
        }
    }
}
