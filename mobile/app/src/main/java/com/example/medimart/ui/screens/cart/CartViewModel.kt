package com.example.medimart.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.local.CartEntity
import com.example.medimart.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {
    val cartItems = cartRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _cartAdded = Channel<Unit>(Channel.BUFFERED)
    val cartAdded = _cartAdded.receiveAsFlow()

    fun addToCart(item: CartEntity) = viewModelScope.launch {
        cartRepository.addToCart(item)
        _cartAdded.send(Unit)
    }

    fun updateQuantity(productId: String, quantity: Int) = viewModelScope.launch { cartRepository.updateQuantity(productId, quantity) }
    fun removeItem(productId: String) = viewModelScope.launch { cartRepository.removeItem(productId) }
    fun clearCart() = viewModelScope.launch { cartRepository.clearCart() }
}
