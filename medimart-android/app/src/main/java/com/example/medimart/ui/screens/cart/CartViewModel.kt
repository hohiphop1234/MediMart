package com.example.medimart.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.local.CartEntity
import com.example.medimart.data.repository.CartRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {
    val cartItems = cartRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addToCart(item: CartEntity) = viewModelScope.launch { cartRepository.addToCart(item) }
    fun updateQuantity(productId: String, quantity: Int) = viewModelScope.launch { cartRepository.updateQuantity(productId, quantity) }
    fun removeItem(productId: String) = viewModelScope.launch { cartRepository.removeItem(productId) }
    fun clearCart() = viewModelScope.launch { cartRepository.clearCart() }
}
