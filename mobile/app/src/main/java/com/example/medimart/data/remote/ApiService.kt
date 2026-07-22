package com.example.medimart.data.remote

import com.example.medimart.data.model.*
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Map<String, String>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshSession(@Body request: RefreshRequest): AuthResponse

    @GET("banners")
    suspend fun getBanners(): List<Banner>

    @GET("categories")
    suspend fun getCategories(): List<Category>

    @GET("products/flash-sale")
    suspend fun getFlashSale(): FlashSaleResponse

    @GET("products/best-seller")
    suspend fun getBestSellers(): List<Product>

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("categoryId") categoryId: String? = null
    ): List<Product>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Product

    @GET("users/profile")
    suspend fun getProfile(): User

    @PUT("users/profile")
    suspend fun updateProfile(@Body user: User): User

    @GET("users/addresses")
    suspend fun getAddresses(): List<Address>

    @POST("users/addresses")
    suspend fun addAddress(@Body request: AddressRequest): Address

    @DELETE("users/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Map<String, Boolean>

    @Streaming
    @POST("chat")
    suspend fun streamChat(@Body request: com.example.medimart.data.model.ChatRequest): okhttp3.ResponseBody

    @Multipart
    @POST("ocr/prescription")
    suspend fun scanPrescription(@Part image: okhttp3.MultipartBody.Part): List<Product>

    @POST("orders/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponse

    @GET("orders/me")
    suspend fun getMyOrders(@Query("status") status: String? = null): List<Order>

    @GET("orders/me/{id}")
    suspend fun getMyOrderById(@Path("id") id: String): OrderDetail

    @PATCH("orders/me/{id}/cancel")
    suspend fun cancelMyOrder(@Path("id") id: String): OrderDetail
}
