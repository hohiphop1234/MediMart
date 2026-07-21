package com.example.medimart.ui.screens.product

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.medimart.data.model.Product
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartOrangeSoft
import com.example.medimart.theme.MediMartTextPrimary
import com.example.medimart.theme.MediMartTextSecondary
import com.example.medimart.ui.components.HtmlText
import com.example.medimart.ui.components.RemoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit,
    onAddToCart: (Product) -> Unit
) {
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(productId) { viewModel.loadProduct(productId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm", color = MediMartTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = MediMartBg
    ) { padding ->
        when {
            isLoading && product == null -> LoadingProductDetail(Modifier.padding(padding))
            product != null -> ProductDetailContent(
                product = product!!,
                modifier = Modifier.padding(padding),
                onAddToCart = onAddToCart
            )
            else -> ProductDetailError(
                message = error ?: "Không tìm thấy sản phẩm",
                modifier = Modifier.padding(padding),
                onRetry = { viewModel.loadProduct(productId) }
            )
        }
    }
}

@Composable
private fun LoadingProductDetail(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MediMartOrange)
    }
}

@Composable
private fun ProductDetailError(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = MediMartTextPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange)
        ) {
            Text("Thử lại")
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: Product,
    modifier: Modifier = Modifier,
    onAddToCart: (Product) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        RemoteImage(
            imageUrl = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(Color.White),
            contentScale = ContentScale.Fit,
            fallbackColor = MediMartOrangeSoft,
            fallbackLabel = product.name.take(1).uppercase()
        )

        Column(modifier = Modifier.padding(20.dp)) {
            if (product.brand.isNotBlank()) {
                Text(
                    text = product.brand,
                    color = MediMartTextSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(6.dp))
            }

            Text(
                text = product.name,
                color = MediMartTextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(16.dp))
            PriceBlock(product)

            if (product.unit.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Đơn vị: ${product.unit}",
                    color = MediMartTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Thông tin sản phẩm",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))
                    HtmlText(
                        html = product.description.ifBlank {
                            "Thông tin chi tiết đang được cập nhật."
                        },
                        color = MediMartTextSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Cam kết từ MediMart",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Sản phẩm có nguồn gốc rõ ràng. Hãy đọc kỹ hướng dẫn sử dụng trước khi dùng.",
                color = MediMartTextSecondary
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onAddToCart(product) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Thêm vào giỏ hàng",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceBlock(product: Product) {
    val displayedPrice = product.salePrice ?: product.price
    Text(
        text = "%,dđ".format(displayedPrice),
        color = MediMartOrange,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
    )
    if (product.salePrice != null) {
        Text(
            text = "%,dđ".format(product.price),
            color = MediMartTextSecondary,
            textDecoration = TextDecoration.LineThrough,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
