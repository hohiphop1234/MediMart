package com.example.medimart.data.repository

import android.util.Log
import com.example.medimart.data.model.Banner
import com.example.medimart.data.model.Category
import com.example.medimart.data.model.FlashSaleResponse
import com.example.medimart.data.model.Product
import com.example.medimart.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ProductRepository"

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getBanners(): Result<List<Banner>> = try {
        Result.success(apiService.getBanners())
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching banners", e)
        Result.failure(e)
    }

    suspend fun getCategories(): Result<List<Category>> = try {
        Result.success(apiService.getCategories())
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching categories", e)
        Result.failure(e)
    }

    suspend fun getFlashSale(): Result<FlashSaleResponse> = try {
        Result.success(apiService.getFlashSale())
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching flash sale", e)
        Result.failure(e)
    }

    suspend fun getBestSellers(): Result<List<Product>> = try {
        Result.success(apiService.getBestSellers())
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching best sellers", e)
        Result.failure(e)
    }

    suspend fun searchProducts(
        query: String,
        categoryId: String? = null,
        sortBy: String = "relevance",
        page: Int? = null,
        limit: Int? = null
    ): Result<List<Product>> = try {
        Result.success(apiService.searchProducts(query, categoryId, sortBy, page, limit))
    } catch (e: Exception) {
        Log.e(TAG, "Error searching products: query=$query, categoryId=$categoryId, sortBy=$sortBy, page=$page, limit=$limit", e)
        Result.failure(e)
    }

    suspend fun getProductById(id: String): Result<Product> = try {
        Result.success(apiService.getProductById(id))
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching product by id: $id", e)
        Result.failure(e)
    }

    suspend fun scanPrescription(image: okhttp3.MultipartBody.Part): Result<List<Product>> = try {
        Result.success(apiService.scanPrescription(image))
    } catch (e: Exception) {
        Log.e(TAG, "Error scanning prescription image", e)
        Result.failure(e)
    }
}
