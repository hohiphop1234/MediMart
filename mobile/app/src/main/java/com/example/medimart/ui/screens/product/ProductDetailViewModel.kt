package com.example.medimart.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.Product
import com.example.medimart.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _product = MutableStateFlow<Product?>(null)
    val product = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadProduct(id: String) {
        if (id.isBlank()) {
            _product.value = null
            _error.value = "Không tìm thấy sản phẩm"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _product.value = null
            productRepository.getProductById(id)
                .onSuccess { _product.value = it }
                .onFailure { _error.value = it.message ?: "Không thể tải thông tin sản phẩm" }
            _isLoading.value = false
        }
    }
}
