package com.example.medimart.ui.screens.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.medimart.data.model.OrderDetail
import com.example.medimart.data.model.OrderItem
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartTextPrimary
import com.example.medimart.theme.MediMartTextSecondary
import com.example.medimart.ui.components.RemoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrdersViewModel,
    onBack: () -> Unit
) {
    val order by viewModel.orderDetail.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()
    val error by viewModel.detailError.collectAsState()
    val isCancelling by viewModel.isCancelling.collectAsState()
    val cancelError by viewModel.cancelError.collectAsState()
    val cancelSuccessMessage by viewModel.cancelSuccessMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCancelDialog by rememberSaveable(orderId) { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(orderId)
    }

    LaunchedEffect(cancelSuccessMessage) {
        cancelSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeCancelSuccessMessage()
        }
    }

    if (showCancelDialog) {
        CancelOrderDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                showCancelDialog = false
                viewModel.cancelOrder(orderId)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MediMartBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    color = MediMartOrange,
                    modifier = Modifier.align(Alignment.Center)
                )
                error != null -> DetailErrorState(
                    message = error.orEmpty(),
                    onRetry = { viewModel.loadOrderDetail(orderId) },
                    modifier = Modifier.align(Alignment.Center)
                )
                order != null -> OrderDetailContent(
                    order = order!!,
                    isCancelling = isCancelling,
                    cancelError = cancelError,
                    onCancel = {
                        viewModel.clearCancelError()
                        showCancelDialog = true
                    }
                )
            }
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: OrderDetail,
    isCancelling: Boolean,
    cancelError: String?,
    onCancel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { OrderStatusCard(order) }
        item { OrderProgressCard(order.status) }
        item {
            SectionTitle(icon = Icons.Default.Receipt, title = "Sản phẩm (${order.itemCount})")
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    order.items.forEachIndexed { index, item ->
                        OrderProductRow(item)
                        if (index < order.items.lastIndex) {
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                }
            }
        }
        item {
            SectionTitle(icon = Icons.Default.LocationOn, title = "Địa chỉ nhận hàng")
        }
        item {
            InfoCard {
                Text(
                    text = "${order.address.name} · ${order.address.phone}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MediMartTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(order.address.address, color = MediMartTextSecondary)
            }
        }
        item {
            SectionTitle(icon = Icons.Default.Payments, title = "Thanh toán")
        }
        item {
            InfoCard {
                Text(
                    text = paymentMethodLabel(order.paymentMethod),
                    color = MediMartTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        item { OrderTotalCard(order) }
        if (order.status.uppercase() == "PENDING") {
            item {
                CancelOrderSection(
                    isCancelling = isCancelling,
                    error = cancelError,
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun OrderStatusCard(order: OrderDetail) {
    val status = orderStatusUi(order.status)
    val icon = when (order.status.uppercase()) {
        "PENDING" -> Icons.Default.AccessTime
        "SHIPPING" -> Icons.Default.LocalShipping
        "DELIVERED" -> Icons.Default.CheckCircle
        "RETURNED" -> Icons.Default.Replay
        "CANCELLED" -> Icons.Default.Cancel
        else -> Icons.Default.Receipt
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = status.background),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = Color.White, shape = CircleShape) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = status.foreground,
                    modifier = Modifier.padding(12.dp).size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = status.foreground
                )
                Text(
                    text = "Đơn #${shortOrderCode(order._id)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = status.foreground.copy(alpha = 0.85f)
                )
                Text(
                    text = "Đặt lúc ${formatOrderDate(order.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = status.foreground.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun OrderProgressCard(status: String) {
    val normalizedStatus = status.uppercase()
    if (normalizedStatus == "CANCELLED" || normalizedStatus == "RETURNED") return

    val currentStep = when (normalizedStatus) {
        "PENDING" -> 0
        "SHIPPING" -> 1
        "DELIVERED" -> 2
        else -> 0
    }
    val labels = listOf("Chờ xác nhận", "Đang giao", "Đã giao")

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text("Tiến trình đơn hàng", fontWeight = FontWeight.Bold, color = MediMartTextPrimary)
            Spacer(modifier = Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                labels.forEachIndexed { index, _ ->
                    ProgressDot(active = index <= currentStep, completed = index < currentStep)
                    if (index < labels.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (index < currentStep) MediMartOrange else Color(0xFFE2E8F0))
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MediMartTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressDot(active: Boolean, completed: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(if (active) MediMartOrange else Color(0xFFE2E8F0), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (active) Color.White else Color(0xFF94A3B8), CircleShape)
            )
        }
    }
}

@Composable
private fun OrderProductRow(item: OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RemoteImage(
            imageUrl = item.imageUrl,
            contentDescription = item.productName,
            modifier = Modifier.size(68.dp),
            contentScale = ContentScale.Fit,
            fallbackColor = Color(0xFFFFF3E0)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MediMartTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${formatMoney(item.unitPrice)} × ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MediMartTextSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatMoney(item.unitPrice * item.quantity),
            fontWeight = FontWeight.Bold,
            color = MediMartTextPrimary
        )
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MediMartOrange, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

@Composable
private fun OrderTotalCard(order: OrderDetail) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tạm tính (${order.itemCount} sản phẩm)", color = MediMartTextSecondary)
                Text(formatMoney(order.totalAmount), color = MediMartTextPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Phí vận chuyển", color = MediMartTextSecondary)
                Text("0 đ", color = MediMartTextPrimary)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFE2E8F0))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tổng thanh toán", fontWeight = FontWeight.Bold, color = MediMartTextPrimary)
                Text(
                    formatMoney(order.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MediMartOrange
                )
            }
        }
    }
}

@Composable
private fun CancelOrderSection(
    isCancelling: Boolean,
    error: String?,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onCancel,
            enabled = !isCancelling,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFDC2626)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
        ) {
            if (isCancelling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFFDC2626),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang hủy đơn...")
            } else {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hủy đơn hàng", fontWeight = FontWeight.Bold)
            }
        }
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CancelOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Cancel,
                contentDescription = null,
                tint = Color(0xFFDC2626)
            )
        },
        title = { Text("Hủy đơn hàng?", fontWeight = FontWeight.Bold) },
        text = {
            Text("Bạn có chắc muốn hủy đơn này? Thao tác này không thể hoàn tác.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Xác nhận hủy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Giữ đơn hàng")
            }
        }
    )
}

@Composable
private fun DetailErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Không thể tải chi tiết đơn hàng",
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
