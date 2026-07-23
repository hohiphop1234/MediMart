package com.example.medimart.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.Product
import com.example.medimart.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _sortBy = MutableStateFlow("relevance")
    val sortBy = _sortBy.asStateFlow()

    private var searchJob: Job? = null
    private var lastQuery = ""
    private var lastCategoryId: String? = null

    fun loadProducts(
        query: String = lastQuery,
        categoryId: String? = lastCategoryId,
        sortBy: String = _sortBy.value
    ) {
        lastQuery = query.trim()
        lastCategoryId = categoryId?.takeIf(String::isNotBlank)
        _sortBy.value = sortBy

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _products.value = emptyList()
            productRepository.searchProducts(lastQuery, lastCategoryId, _sortBy.value)
                .onSuccess { _products.value = it }
                .onFailure {
                    _error.value = it.message ?: "Không thể tải danh sách sản phẩm"
                }
            _isLoading.value = false
        }
    }

    fun setSortBy(newSortBy: String) {
        if (_sortBy.value != newSortBy) {
            loadProducts(lastQuery, lastCategoryId, newSortBy)
        }
    }

    fun retry() = loadProducts(lastQuery, lastCategoryId, _sortBy.value)
}
