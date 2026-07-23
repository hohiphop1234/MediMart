package com.example.medimart.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.Banner
import com.example.medimart.data.model.Category
import com.example.medimart.data.model.Product
import com.example.medimart.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners = _banners.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _flashSaleProducts = MutableStateFlow<List<Product>>(emptyList())
    val flashSaleProducts = _flashSaleProducts.asStateFlow()
    
    private val _flashSaleEndTime = MutableStateFlow("")
    val flashSaleEndTime = _flashSaleEndTime.asStateFlow()

    private val _bestSellers = MutableStateFlow<List<Product>>(emptyList())
    val bestSellers = _bestSellers.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            listOf(
                async {
                    productRepository.getBanners()
                        .onSuccess { _banners.value = it }
                        .onFailure { _error.value = it.message ?: "Không tải được banner" }
                },
                async {
                    productRepository.getCategories()
                        .onSuccess { _categories.value = it }
                        .onFailure { _error.value = it.message ?: "Không tải được danh mục" }
                },
                async {
                    productRepository.getFlashSale()
                        .onSuccess {
                            _flashSaleProducts.value = it.products
                            _flashSaleEndTime.value = it.endTime
                        }
                        .onFailure { _error.value = it.message ?: "Không tải được Flash Sale" }
                },
                async {
                    productRepository.getBestSellers()
                        .onSuccess { _bestSellers.value = it }
                        .onFailure { _error.value = it.message ?: "Không tải được sản phẩm" }
                }
            ).awaitAll()

            _isLoading.value = false
        }
    }
}
