package com.example.medimart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationSheet(
    onDismissRequest: () -> Unit,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cần tư vấn từ Dược sĩ?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MediMartTextPrimary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dược sĩ chuyên môn của Medi Mart luôn sẵn sàng hỗ trợ bạn 24/7",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    onCallClick()
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Gọi điện", tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Gọi Tổng Đài (Miễn phí)", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = {
                    onMessageClick()
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MediMartOrange),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MediMartOrange)
            ) {
                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Nhắn tin", tint = MediMartOrange)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Chat với Dược sĩ", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
