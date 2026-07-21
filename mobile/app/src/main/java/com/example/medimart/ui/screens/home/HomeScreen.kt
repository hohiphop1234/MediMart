package com.example.medimart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medimart.data.model.Category
import com.example.medimart.data.model.Product
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartTextPrimary
import com.example.medimart.ui.components.BannerSlider
import com.example.medimart.ui.components.CategoryIcon
import com.example.medimart.ui.components.CountdownTimer
import com.example.medimart.ui.components.ProductCard
import com.example.medimart.ui.components.SearchBar
import kotlin.math.ceil

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onProductClick: (Product) -> Unit,
    onAddToCartClick: (Product) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onSearch: (String) -> Unit
) {
    val banners by viewModel.banners.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val flashSaleProducts by viewModel.flashSaleProducts.collectAsState()
    val flashSaleEndTime by viewModel.flashSaleEndTime.collectAsState()
    val bestSellers by viewModel.bestSellers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    if (isLoading && banners.isEmpty() && categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(MediMartBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MediMartOrange)
        }
        return
    }

    if (error != null && banners.isEmpty() && categories.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MediMartBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Không thể tải trang chủ",
                    style = MaterialTheme.typography.titleLarge,
                    color = MediMartTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(error ?: "Vui lòng thử lại", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = viewModel::loadHomeData,
                    colors = ButtonDefaults.buttonColors(containerColor = MediMartOrange)
                ) {
                    Text("Thử lại")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediMartBg)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.background(MediMartBg).padding(top = 8.dp)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    searchQuery.trim().takeIf(String::isNotEmpty)?.let(onSearch)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        BannerSlider(banners = banners)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Danh mục nổi bật",
            style = MaterialTheme.typography.titleLarge.copy(color = MediMartTextPrimary),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        val categoryRows = maxOf(1, ceil(categories.size / 4.0).toInt())
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .height((categoryRows * 120).dp)
                .padding(horizontal = 16.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onCategoryClick(category) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CategoryIcon(
                        categoryName = category.name,
                        contentDescription = category.name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (flashSaleProducts.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFFFFF0F0), RoundedCornerShape(24.dp))
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚡ Flash Sale", color = Color(0xFFE53935), style = MaterialTheme.typography.headlineMedium)
                        CountdownTimer(endTimeString = flashSaleEndTime)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(flashSaleProducts) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                onAddToCartClick = onAddToCartClick
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Gợi ý cho bạn",
            style = MaterialTheme.typography.titleLarge.copy(color = MediMartTextPrimary),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(bestSellers) { product ->
                ProductCard(
                    product = product,
                    onProductClick = onProductClick,
                    onAddToCartClick = onAddToCartClick
                )
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for Bottom Nav
    }
}
