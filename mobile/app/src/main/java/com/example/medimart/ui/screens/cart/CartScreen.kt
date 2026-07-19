package com.example.medimart.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.ui.components.EmptyState
import com.example.medimart.ui.components.RemoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: CartViewModel, onNavigateToHome: () -> Unit, onNavigateToCheckout: () -> Unit) {
    val items by viewModel.cartItems.collectAsState()
    val totalAmount = items.sumOf { it.price * it.quantity }

    Column(modifier = Modifier.fillMaxSize().background(MediMartBg)) {
        CenterAlignedTopAppBar(
            title = { Text("Giỏ hàng", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )
        if (items.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "Giỏ hàng rỗng",
                    message = "Bạn chưa chọn sản phẩm nào",
                    buttonText = "Tiếp tục mua sắm",
                    onButtonClick = onNavigateToHome
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RemoteImage(
                                imageUrl = item.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                fallbackColor = Color(0xFFFFD9A0)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("%,d đ".format(item.price), color = MediMartOrange, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.updateQuantity(item.productId, item.quantity - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                Text("${item.quantity}", style = MaterialTheme.typography.bodyLarge)
                                IconButton(onClick = { viewModel.updateQuantity(item.productId, item.quantity + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                    }
                }
            }
            Surface(shadowElevation = 16.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tổng cộng", color = Color.Gray)
                        Text("%,d đ".format(totalAmount), color = MediMartOrange, style = MaterialTheme.typography.titleLarge)
                    }
                    Button(
                        onClick = onNavigateToCheckout,
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange),
                        modifier = Modifier.height(50.dp).padding(horizontal = 16.dp)
                    ) {
                        Text("Mua hàng", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }
        }
    }
}
