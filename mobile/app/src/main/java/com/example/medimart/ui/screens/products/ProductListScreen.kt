package com.example.medimart.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medimart.data.model.Product
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartTextPrimary
import com.example.medimart.theme.MediMartTextSecondary
import com.example.medimart.ui.components.ProductCard
import com.example.medimart.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    title: String,
    initialQuery: String,
    categoryId: String?,
    viewModel: ProductListViewModel,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCartClick: (Product) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentSortBy by viewModel.sortBy.collectAsState()
    var query by rememberSaveable(initialQuery, categoryId) { mutableStateOf(initialQuery) }

    LaunchedEffect(initialQuery, categoryId) {
        viewModel.loadProducts(initialQuery, categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        color = MediMartTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MediMartBg)
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { viewModel.loadProducts(query, categoryId) },
                placeholder = if (categoryId == null) {
                    "Tìm thuốc, thương hiệu, công dụng..."
                } else {
                    "Tìm trong danh mục này..."
                }
            )

            SortChipsBar(
                currentSortBy = currentSortBy,
                onSortSelected = { newSortBy ->
                    viewModel.setSortBy(newSortBy)
                }
            )

            when {
                isLoading -> ProductListLoading()
                error != null -> ProductListError(error.orEmpty(), viewModel::retry)
                products.isEmpty() -> ProductListEmpty(query)
                else -> {
                    Text(
                        text = "${products.size} sản phẩm",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MediMartTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(products, key = { it._id }) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                onAddToCartClick = onAddToCartClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortChipsBar(
    currentSortBy: String,
    onSortSelected: (String) -> Unit
) {
    val options = listOf(
        Triple("relevance", "Liên quan", Icons.Default.SwapVert),
        Triple("price_asc", "Giá thấp -> cao", Icons.Default.ArrowUpward),
        Triple("price_desc", "Giá cao -> thấp", Icons.Default.ArrowDownward)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (key, label, icon) ->
            val isSelected = currentSortBy == key
            FilterChip(
                selected = isSelected,
                onClick = { onSortSelected(key) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MediMartOrange.copy(alpha = 0.15f),
                    selectedLabelColor = MediMartOrange,
                    selectedLeadingIconColor = MediMartOrange,
                    containerColor = Color.White,
                    labelColor = MediMartTextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = MediMartOrange,
                    borderColor = Color(0xFFE0E0E0),
                    selectedBorderWidth = 1.5.dp
                )
            )
        }
    }
}

@Composable
private fun ProductListLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MediMartOrange)
    }
}

@Composable
private fun ProductListError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MediMartTextPrimary,
            textAlign = TextAlign.Center
        )
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
private fun ProductListEmpty(query: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MediMartTextSecondary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (query.isBlank()) {
                "Danh mục này chưa có sản phẩm"
            } else {
                "Không tìm thấy sản phẩm phù hợp"
            },
            color = MediMartTextPrimary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (query.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Thử từ khóa ngắn hơn hoặc kiểm tra lại chính tả.",
                color = MediMartTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
