package com.example.medimart.data.remote

import com.example.medimart.data.model.*
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Map<String, String>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): AuthResponse

    @GET("banners")
    suspend fun getBanners(): List<Banner>

    @GET("categories")
    suspend fun getCategories(): List<Category>

    @GET("products/flash-sale")
    suspend fun getFlashSale(): FlashSaleResponse

    @GET("products/best-seller")
    suspend fun getBestSellers(): List<Product>

    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): List<Product>

    @GET("users/profile")
    suspend fun getProfile(): User

    @PUT("users/profile")
    suspend fun updateProfile(@Body user: User): User

    @GET("users/me/points")
    suspend fun getMyPoints(): Map<String, Int>

    @GET("users/addresses")
    suspend fun getAddresses(): List<Address>

    @POST("users/addresses")
    suspend fun addAddress(@Body address: Address): Address

    @DELETE("users/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Map<String, Boolean>

    @GET("rewards")
    suspend fun getRewards(@Query("maxPoints") maxPoints: Int? = null): List<Product>

    @POST("rewards/redeem")
    suspend fun redeemReward(@Body request: Map<String, String>): Map<String, Any>

    @POST("orders/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponse

    @GET("orders/my-orders")
    suspend fun getMyOrders(@Query("status") status: String? = null): List<Order>
}
