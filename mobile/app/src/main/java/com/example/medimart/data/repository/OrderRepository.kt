package com.example.medimart.data.repository

import android.util.Log
import com.example.medimart.data.model.CheckoutRequest
import com.example.medimart.data.model.CheckoutResponse
import com.example.medimart.data.model.Order
import com.example.medimart.data.model.OrderDetail
import com.example.medimart.data.remote.ApiService

private const val TAG = "OrderRepository"

class OrderRepository(private val apiService: ApiService) {
    suspend fun checkout(request: CheckoutRequest): Result<CheckoutResponse> = try {
        Result.success(apiService.checkout(request))
    } catch (e: Exception) {
        Log.e(TAG, "Error during checkout", e)
        Result.failure(e)
    }

    suspend fun getMyOrders(status: String? = null): Result<List<Order>> = try {
        Result.success(apiService.getMyOrders(status))
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching my orders", e)
        Result.failure(e)
    }

    suspend fun getMyOrderById(id: String): Result<OrderDetail> = try {
        Result.success(apiService.getMyOrderById(id))
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching order by id: $id", e)
        Result.failure(e)
    }

    suspend fun cancelMyOrder(id: String): Result<OrderDetail> = try {
        Result.success(apiService.cancelMyOrder(id))
    } catch (e: Exception) {
        Log.e(TAG, "Error canceling order: $id", e)
        Result.failure(e)
    }
}
