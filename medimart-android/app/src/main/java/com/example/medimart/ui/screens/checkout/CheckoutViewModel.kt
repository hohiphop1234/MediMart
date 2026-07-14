package com.example.medimart.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.Address
import com.example.medimart.data.model.CheckoutItem
import com.example.medimart.data.model.CheckoutRequest
import com.example.medimart.data.repository.CartRepository
import com.example.medimart.data.repository.OrderRepository
import com.example.medimart.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val cartItems = cartRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses = _addresses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _checkoutSuccess = MutableStateFlow(false)
    val checkoutSuccess = _checkoutSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getAddresses().onSuccess { _addresses.value = it }
        }
    }

    fun checkout(addressId: String, paymentMethod: String) {
        val items = cartItems.value
        if (items.isEmpty()) {
            _error.value = "Giỏ hàng rỗng"
            return
        }

        val requestItems = items.map { CheckoutItem(it.productId, it.quantity) }
        val request = CheckoutRequest(requestItems, addressId, paymentMethod)

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            orderRepository.checkout(request).fold(
                onSuccess = {
                    cartRepository.clearCart()
                    _checkoutSuccess.value = true
                },
                onFailure = {
                    _error.value = it.message ?: "Lỗi đặt hàng"
                }
            )
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
