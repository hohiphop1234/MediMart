package com.example.medimart.data.model

data class User(
    val _id: String,
    val name: String,
    val email: String
)

data class Product(
    val _id: String,
    val name: String,
    val description: String,
    val price: Int,
    val salePrice: Int?,
    val unit: String,
    val imageUrl: String,
    val categoryId: String,
    val brand: String,
    val isFlashSale: Boolean,
    val isBestSeller: Boolean
)

data class Category(
    val _id: String,
    val name: String,
    val icon: String,
    val productCount: Int
)

data class Banner(
    val _id: String,
    val imageUrl: String,
    val linkTo: String
)

data class Address(
    val _id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val address: String,
    val isDefault: Boolean
)

data class AddressRequest(
    val name: String,
    val phone: String,
    val address: String,
    val isDefault: Boolean
)

data class Order(
    val _id: String,
    val totalAmount: Int,
    val status: String,
    val paymentMethod: String,
    val createdAt: String,
    val updatedAt: String = "",
    val itemCount: Int = 0,
    val previewItems: List<String> = emptyList(),
    val hasMoreItems: Boolean = false
)

data class OrderItem(
    val _id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Int,
    val imageUrl: String
)

data class OrderAddress(
    val name: String,
    val phone: String,
    val address: String
)

data class OrderDetail(
    val _id: String,
    val totalAmount: Int,
    val status: String,
    val paymentMethod: String,
    val createdAt: String,
    val updatedAt: String,
    val itemCount: Int,
    val previewItems: List<String>,
    val hasMoreItems: Boolean,
    val address: OrderAddress,
    val items: List<OrderItem>
)

data class LoginRequest(val email: String)
data class OtpRequest(val email: String, val otp: String)
data class RefreshRequest(val refreshToken: String)
data class AuthResponse(
    val token: String,
    val refreshToken: String?,
    val expiresAt: Long?,
    val user: User
)

data class FlashSaleResponse(
    val products: List<Product>,
    val endTime: String
)

data class CheckoutItem(
    val productId: String,
    val quantity: Int
)

data class CheckoutRequest(
    val items: List<CheckoutItem>,
    val addressId: String,
    val paymentMethod: String
)

data class CheckoutResponse(
    val orderId: String,
    val totalAmount: Int,
    val status: String
)

data class ChatRequest(val message: String)
data class ChatResponse(val reply: String)
data class ChatMessage(val id: String, val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())
