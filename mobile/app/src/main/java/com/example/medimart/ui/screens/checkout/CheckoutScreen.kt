package com.example.medimart.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartDisabledContent
import com.example.medimart.theme.MediMartDisabledSurface
import com.example.medimart.theme.MediMartOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onCheckoutSuccess: () -> Unit
) {
    val items by viewModel.cartItems.collectAsState()
    val addresses by viewModel.addresses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val checkoutSuccess by viewModel.checkoutSuccess.collectAsState()

    val totalAmount = items.sumOf { it.price * it.quantity }
    val defaultAddress = addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
    val canCheckout = !isLoading && defaultAddress != null && items.isNotEmpty()

    LaunchedEffect(checkoutSuccess) {
        if (checkoutSuccess) {
            onCheckoutSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cần thanh toán", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        Text("%,d đ".format(totalAmount), color = MediMartOrange, style = MaterialTheme.typography.titleLarge)
                    }
                    Button(
                        onClick = {
                            if (defaultAddress != null) {
                                viewModel.checkout(defaultAddress._id, "COD")
                            }
                        },
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MediMartOrange,
                            disabledContainerColor = MediMartDisabledSurface,
                            disabledContentColor = MediMartDisabledContent
                        ),
                        modifier = Modifier.height(50.dp).padding(horizontal = 16.dp),
                        enabled = canCheckout
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = MediMartDisabledContent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                "Đặt hàng",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (canCheckout) Color.White else MediMartDisabledContent
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MediMartBg)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text("Địa chỉ nhận hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MediMartOrange)
                    Spacer(modifier = Modifier.width(16.dp))
                    if (isLoading && addresses.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MediMartOrange,
                            strokeWidth = 2.dp
                        )
                    } else if (defaultAddress != null) {
                        Column {
                            Text("${defaultAddress.name} - ${defaultAddress.phone}", fontWeight = FontWeight.Bold)
                            Text(defaultAddress.address, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Chưa có địa chỉ giao hàng", color = Color.Gray)
                            TextButton(onClick = viewModel::loadAddresses) { Text("Tải lại") }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Phương thức thanh toán", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = MediMartOrange)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Thanh toán tiền mặt khi nhận hàng (COD)", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Chi tiết đơn hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tạm tính (${items.sumOf { it.quantity }} sản phẩm)", color = Color.Gray)
                        Text("%,d đ".format(totalAmount), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Phí vận chuyển", color = Color.Gray)
                        Text("0 đ", fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng cộng", fontWeight = FontWeight.Bold)
                        Text("%,d đ".format(totalAmount), color = MediMartOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
