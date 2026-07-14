package com.example.medimart.data.repository

import com.example.medimart.data.model.Banner
import com.example.medimart.data.model.Category
import com.example.medimart.data.model.FlashSaleResponse
import com.example.medimart.data.model.Product
import com.example.medimart.data.remote.ApiService

class ProductRepository(private val apiService: ApiService) {
    suspend fun getBanners(): Result<List<Banner>> = try {
        Result.success(apiService.getBanners())
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getCategories(): Result<List<Category>> = try {
        Result.success(apiService.getCategories())
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getFlashSale(): Result<FlashSaleResponse> = try {
        Result.success(apiService.getFlashSale())
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getBestSellers(): Result<List<Product>> = try {
        Result.success(apiService.getBestSellers())
    } catch (e: Exception) { Result.failure(e) }

    suspend fun searchProducts(query: String): Result<List<Product>> = try {
        Result.success(apiService.searchProducts(query))
    } catch (e: Exception) { Result.failure(e) }
}
