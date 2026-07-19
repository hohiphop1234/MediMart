package com.example.medimart.data.model

data class User(
    val _id: String,
    val name: String,
    val phone: String,
    val loyaltyPoints: Int
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
    val isBestSeller: Boolean,
    val isRewardItem: Boolean,
    val pointPrice: Int
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

data class Order(
    val _id: String,
    val totalAmount: Int,
    val status: String,
    val paymentMethod: String,
    val createdAt: String
)

data class LoginRequest(val phone: String)
data class OtpRequest(val phone: String, val otp: String)
data class AuthResponse(val token: String, val user: User)

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
