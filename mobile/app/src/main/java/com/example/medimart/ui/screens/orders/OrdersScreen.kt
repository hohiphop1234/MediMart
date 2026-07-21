package com.example.medimart.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.medimart.data.model.Order
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartTextPrimary
import com.example.medimart.theme.MediMartTextSecondary

private data class OrderFilter(val label: String, val status: String?)

private val orderFilters = listOf(
    OrderFilter("Tất cả", null),
    OrderFilter("Chờ xác nhận", "PENDING"),
    OrderFilter("Đang giao", "SHIPPING"),
    OrderFilter("Đã giao", "DELIVERED"),
    OrderFilter("Đã hủy", "CANCELLED"),
    OrderFilter("Hoàn trả", "RETURNED")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.listError.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng của tôi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOrders() }, enabled = !isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = MediMartBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orderFilters) { filter ->
                    FilterChip(
                        selected = selectedStatus == filter.status,
                        onClick = { viewModel.selectStatus(filter.status) },
                        label = { Text(filter.label) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && orders.isEmpty() -> CircularProgressIndicator(
                        color = MediMartOrange,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    error != null && orders.isEmpty() -> OrdersErrorState(
                        message = error.orEmpty(),
                        onRetry = { viewModel.loadOrders() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                    orders.isEmpty() -> EmptyOrdersState(
                        filtered = selectedStatus != null,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders, key = { it._id }) { order ->
                            OrderCard(order = order, onClick = { onOrderClick(order._id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    val status = orderStatusUi(order.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đơn #${shortOrderCode(order._id)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MediMartTextPrimary
                    )
                    Text(
                        text = formatOrderDate(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MediMartTextSecondary
                    )
                }
                Surface(color = status.background, shape = RoundedCornerShape(50)) {
                    Text(
                        text = status.label,
                        color = status.foreground,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (order.previewItems.isNotEmpty()) {
                order.previewItems.forEach { productName ->
                    Text(
                        text = "• $productName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediMartTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (order.hasMoreItems) {
                    Text(
                        text = "và các sản phẩm khác",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediMartTextSecondary
                    )
                }
            } else {
                Text(
                    text = "${order.itemCount} sản phẩm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediMartTextSecondary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFE2E8F0))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.itemCount} sản phẩm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediMartTextSecondary
                    )
                    Text(
                        text = formatMoney(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MediMartOrange
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Xem chi tiết", color = MediMartOrange, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MediMartOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyOrdersState(filtered: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Color(0xFFFFEDD5), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = MediMartOrange,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (filtered) "Không có đơn hàng ở trạng thái này" else "Bạn chưa có đơn hàng nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MediMartTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (filtered) "Hãy chọn trạng thái khác để xem đơn hàng." else "Đơn hàng sau khi đặt sẽ xuất hiện tại đây.",
            style = MaterialTheme.typography.bodyMedium,
            color = MediMartTextSecondary
        )
    }
}

@Composable
private fun OrdersErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Không thể tải đơn hàng",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MediMartTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(message, color = MediMartTextSecondary)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange)
        ) {
            Text("Thử lại")
        }
    }
}
