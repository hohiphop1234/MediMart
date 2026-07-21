package com.example.medimart.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.medimart.data.model.Address
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartDisabledContent
import com.example.medimart.theme.MediMartDisabledSurface
import com.example.medimart.theme.MediMartOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onCheckoutSuccess: (String) -> Unit
) {
    val items by viewModel.cartItems.collectAsState()
    val addresses by viewModel.addresses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSavingAddress by viewModel.isSavingAddress.collectAsState()
    val error by viewModel.error.collectAsState()
    val addressError by viewModel.addressError.collectAsState()
    val createdOrderId by viewModel.createdOrderId.collectAsState()

    var showAddAddressDialog by rememberSaveable { mutableStateOf(false) }
    var selectedAddressId by rememberSaveable { mutableStateOf<String?>(null) }

    val totalAmount = items.sumOf { it.price * it.quantity }
    LaunchedEffect(addresses) {
        if (addresses.none { it._id == selectedAddressId }) {
            selectedAddressId = addresses.firstOrNull { it.isDefault }?._id
                ?: addresses.firstOrNull()?._id
        }
    }

    val selectedAddress = addresses.firstOrNull { it._id == selectedAddressId }
        ?: addresses.firstOrNull { it.isDefault }
        ?: addresses.firstOrNull()
    val canCheckout = !isLoading && selectedAddress != null && items.isNotEmpty()

    LaunchedEffect(createdOrderId) {
        createdOrderId?.let { orderId ->
            onCheckoutSuccess(orderId)
            viewModel.consumeCreatedOrder()
        }
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            isSaving = isSavingAddress,
            error = addressError,
            forceDefault = addresses.isEmpty(),
            onDismiss = {
                if (!isSavingAddress) {
                    viewModel.clearAddressError()
                    showAddAddressDialog = false
                }
            },
            onSave = { name, phone, address, isDefault ->
                viewModel.addAddress(name, phone, address, isDefault) { created ->
                    selectedAddressId = created._id
                    showAddAddressDialog = false
                }
            }
        )
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
                            if (selectedAddress != null) {
                                viewModel.checkout(selectedAddress._id, "COD")
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Địa chỉ nhận hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(
                    onClick = {
                        viewModel.clearAddressError()
                        showAddAddressDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Thêm địa chỉ")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isLoading && addresses.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = MediMartOrange,
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (addresses.isNotEmpty()) {
                        addresses.forEachIndexed { index, address ->
                            AddressOption(
                                address = address,
                                selected = address._id == selectedAddress?._id,
                                onClick = { selectedAddressId = address._id }
                            )
                            if (index < addresses.lastIndex) {
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MediMartOrange)
                            Spacer(Modifier.height(8.dp))
                            Text("Chưa có địa chỉ giao hàng", color = Color.Gray)
                            TextButton(onClick = { showAddAddressDialog = true }) {
                                Text("Tạo địa chỉ đầu tiên")
                            }
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
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng cộng", fontWeight = FontWeight.Bold)
                        Text("%,d đ".format(totalAmount), color = MediMartOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressOption(
    address: Address,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${address.name} - ${address.phone}", fontWeight = FontWeight.Bold)
                if (address.isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Mặc định",
                        color = MediMartOrange,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(address.address, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
private fun AddAddressDialog(
    isSaving: Boolean,
    error: String?,
    forceDefault: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, isDefault: Boolean) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var isDefault by rememberSaveable(forceDefault) { mutableStateOf(forceDefault) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm địa chỉ giao hàng", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tên người nhận") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.take(20) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.take(300) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Địa chỉ chi tiết") },
                    minLines = 3,
                    maxLines = 5
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !forceDefault) { isDefault = !isDefault },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = if (forceDefault) null else { value -> isDefault = value }
                    )
                    Text(
                        if (forceDefault) "Địa chỉ đầu tiên sẽ là mặc định"
                        else "Đặt làm địa chỉ mặc định"
                    )
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, address, isDefault) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Lưu địa chỉ")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Hủy")
            }
        }
    )
}
