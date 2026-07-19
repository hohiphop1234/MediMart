package com.example.medimart.data.repository

import com.example.medimart.data.model.Product
import com.example.medimart.data.remote.ApiService

class RewardRepository(private val apiService: ApiService) {
    suspend fun getMyPoints(): Result<Int> = try {
        val res = apiService.getMyPoints()
        Result.success(res["points"] ?: 0)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getRewards(maxPoints: Int? = null): Result<List<Product>> = try {
        Result.success(apiService.getRewards(maxPoints))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun redeemReward(rewardId: String): Result<Boolean> = try {
        val res = apiService.redeemReward(mapOf("rewardId" to rewardId))
        Result.success(res["success"] as? Boolean ?: false)
    } catch (e: Exception) { Result.failure(e) }
}
