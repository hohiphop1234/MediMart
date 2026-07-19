package com.example.medimart.data.repository

import com.example.medimart.data.model.CheckoutRequest
import com.example.medimart.data.model.CheckoutResponse
import com.example.medimart.data.model.Order
import com.example.medimart.data.remote.ApiService

class OrderRepository(private val apiService: ApiService) {
    suspend fun checkout(request: CheckoutRequest): Result<CheckoutResponse> = try {
        Result.success(apiService.checkout(request))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getMyOrders(status: String? = null): Result<List<Order>> = try {
        Result.success(apiService.getMyOrders(status))
    } catch (e: Exception) { Result.failure(e) }
}
