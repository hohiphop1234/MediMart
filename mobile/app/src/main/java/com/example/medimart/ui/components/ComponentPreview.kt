package com.example.medimart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medimart.data.model.Banner
import com.example.medimart.data.model.Product
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartTheme

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun AllComponentsPreview() {
    MediMartTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar(currentRoute = "home", onNavigate = {}, cartBadgeCount = 3)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MediMartBg)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Search Bar
                SearchBar(query = "", onQueryChange = {})

                // 2. Banner Slider
                val mockBanners = listOf(
                    Banner("1", "https://placehold.co/800x400/FF9800/FFF?text=Banner+1", "/sale"),
                    Banner("2", "https://placehold.co/800x400/FFB74D/FFF?text=Banner+2", "/news")
                )
                BannerSlider(banners = mockBanners)

                // 3. Countdown Timer
                CountdownTimer(endTimeString = "")

                // 4. Product Card
                val mockProduct = Product(
                    _id = "1",
                    name = "Men vi sinh Enterogermina 2B C/20 (Sanofi)",
                    description = "Hỗ trợ tiêu hóa",
                    price = 156000,
                    salePrice = 150000,
                    unit = "Hộp",
                    imageUrl = "https://placehold.co/300x300?text=SP",
                    categoryId = "cat1",
                    brand = "Sanofi",
                    isFlashSale = true,
                    isBestSeller = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProductCard(product = mockProduct, onProductClick = {}, onAddToCartClick = {})
                    ProductCard(
                        product = mockProduct.copy(isFlashSale = false),
                        onProductClick = {},
                        onAddToCartClick = {}
                    )
                }

                // 5. Empty State
                Box(modifier = Modifier.height(300.dp)) {
                    EmptyState(
                        title = "Giỏ hàng trống",
                        message = "Chưa có sản phẩm nào trong giỏ hàng",
                        buttonText = "Tiếp tục mua sắm",
                        onButtonClick = {}
                    )
                }
            }
        }
    }
}
