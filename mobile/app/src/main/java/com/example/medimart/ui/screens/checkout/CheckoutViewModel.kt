package com.example.medimart.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.Address
import com.example.medimart.data.model.AddressRequest
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

    private val _createdOrderId = MutableStateFlow<String?>(null)
    val createdOrderId = _createdOrderId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isSavingAddress = MutableStateFlow(false)
    val isSavingAddress = _isSavingAddress.asStateFlow()

    private val _addressError = MutableStateFlow<String?>(null)
    val addressError = _addressError.asStateFlow()

    fun loadAddresses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            userRepository.getAddresses().fold(
                onSuccess = { _addresses.value = it },
                onFailure = { _error.value = it.message ?: "Không tải được địa chỉ" }
            )
            _isLoading.value = false
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
                onSuccess = { response ->
                    cartRepository.clearCart()
                    _createdOrderId.value = response.orderId
                },
                onFailure = {
                    _error.value = it.message ?: "Lỗi đặt hàng"
                }
            )
            _isLoading.value = false
        }
    }

    fun addAddress(
        name: String,
        phone: String,
        address: String,
        isDefault: Boolean,
        onSuccess: (Address) -> Unit
    ) {
        val normalizedName = name.trim()
        val normalizedPhone = phone.trim()
        val normalizedAddress = address.trim()
        val phoneDigits = normalizedPhone.filter(Char::isDigit)

        when {
            normalizedName.isBlank() -> {
                _addressError.value = "Vui lòng nhập tên người nhận"
                return
            }
            phoneDigits.length !in 9..11 -> {
                _addressError.value = "Số điện thoại không hợp lệ"
                return
            }
            normalizedAddress.length < 8 -> {
                _addressError.value = "Vui lòng nhập địa chỉ giao hàng đầy đủ"
                return
            }
        }

        viewModelScope.launch {
            _isSavingAddress.value = true
            _addressError.value = null
            val request = AddressRequest(
                name = normalizedName,
                phone = normalizedPhone,
                address = normalizedAddress,
                isDefault = isDefault || _addresses.value.isEmpty()
            )
            userRepository.addAddress(request).fold(
                onSuccess = { created ->
                    val existing = _addresses.value
                        .filterNot { it._id == created._id }
                        .map { item ->
                            if (created.isDefault) item.copy(isDefault = false) else item
                        }
                    _addresses.value = listOf(created) + existing
                    onSuccess(created)
                },
                onFailure = {
                    _addressError.value = it.message ?: "Không thể thêm địa chỉ"
                }
            )
            _isSavingAddress.value = false
        }
    }

    fun clearAddressError() {
        _addressError.value = null
    }

    fun consumeCreatedOrder() {
        _createdOrderId.value = null
    }

    fun clearError() { _error.value = null }
}
